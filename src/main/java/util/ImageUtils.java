package util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

public class ImageUtils {

    public static String imageToPdf(String imgPath, String output) throws Exception {
        // Use the multiple images method for single image (backward compatibility)
        return imagesToPdf(new String[] { imgPath }, output);
    }

    public static String imagesToPdf(String[] imgPaths, String output) throws Exception {
        if (imgPaths == null || imgPaths.length == 0) {
            throw new IllegalArgumentException("At least one image path is required");
        }

        PDDocument doc = new PDDocument();

        try {
            // Process each image and add it as a page
            for (String imgPath : imgPaths) {
                if (imgPath == null || imgPath.trim().isEmpty()) {
                    continue; // Skip empty paths
                }

                try {
                    // Read image with subsampling to prevent OOM on large files
                    File imageFile = new File(imgPath);
                    BufferedImage bufferedImage = readSubsampledImage(imageFile, 2048);

                    // Get actual image dimensions (as uploaded, without EXIF correction)
                    int imgWidth = bufferedImage.getWidth();
                    int imgHeight = bufferedImage.getHeight();

                    // Get EXIF orientation
                    int orientation = getExifOrientation(imageFile);
                    System.out.println("DEBUG: Image " + imgPath);
                    System.out.println("DEBUG: Dimensions " + imgWidth + "x" + imgHeight);
                    System.out.println("DEBUG: Detected EXIF Orientation: " + orientation);

                    // FALLBACK: If no EXIF orientation (=1) and image is landscape with phone
                    // camera aspect ratio
                    // Auto-rotate to portrait since phone photos are typically taken in portrait
                    if (orientation == 1 && imgWidth > imgHeight) {
                        float aspectRatio = (float) imgWidth / imgHeight;
                        // Phone cameras typically use 4:3 (1.33) or 16:9 (1.78) ratios
                        // 4080x3060 = 1.33 (4:3)
                        if (aspectRatio >= 1.3f && aspectRatio <= 1.4f) {
                            System.out.println("DEBUG: Auto-rotating landscape phone photo (4:3 ratio)");
                            orientation = 6; // Rotate 90° CW
                        }
                    }

                    // Determine if image is portrait or landscape based on raw dimensions
                    boolean isPortrait = imgHeight > imgWidth;

                    // Create page with appropriate dimensions matching the RAW image
                    PDPage page;
                    if (isPortrait) {
                        // Portrait: Standard A4 (595 x 842 points)
                        page = new PDPage(new PDRectangle(595, 842));
                    } else {
                        // Landscape: A4 rotated (842 x 595 points)
                        page = new PDPage(new PDRectangle(842, 595));
                    }

                    doc.addPage(page);

                    // Compress, optimize, AND rotate the image logic
                    // This rotates the PIXELS of the scaled down image, so it is memory safe
                    BufferedImage optimizedImage = compressAndOrientImage(bufferedImage, orientation);

                    // Update page orientation based on the FINAL processing image
                    // If the rotation changed the aspect ratio, we might need to swap the page
                    // dimensions
                    // But actually, we already decided page based on RAW dimensions.
                    // If RAW was Landscape (4000x3000) and EXIF was 6 (Rotate 90):
                    // isPortrait (Raw) = false. Page = Landscape.
                    // optimizedImage = Rotated 90 -> Portrait (Height > Width).
                    // So we are putting a Portrait Image on a Landscape Page.
                    // This is WRONG. We want a Portrait Page.

                    // CORRECTION: We should decide page orientation AFTER processing the image.
                    boolean finalIsPortrait = optimizedImage.getHeight() > optimizedImage.getWidth();
                    if (finalIsPortrait) {
                        page.setMediaBox(new PDRectangle(595, 842)); // Set to Portrait
                        page.setRotation(0);
                    } else {
                        page.setMediaBox(new PDRectangle(842, 595)); // Set to Landscape
                        page.setRotation(0);
                    }

                    // Convert optimized image to JPEG for compression, then create PDF image
                    File tempJpeg = null;
                    PDImageXObject img = null;
                    try {
                        tempJpeg = File.createTempFile("pdf_temp_", ".jpg");

                        // Write as JPEG with compression
                        java.util.Iterator<javax.imageio.ImageWriter> writers = javax.imageio.ImageIO
                                .getImageWritersByFormatName("jpeg");

                        if (!writers.hasNext()) {
                            throw new Exception("No JPEG writer available");
                        }

                        javax.imageio.ImageWriter jpegWriter = writers.next();
                        javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO
                                .createImageOutputStream(tempJpeg);
                        jpegWriter.setOutput(ios);

                        // Set JPEG compression quality (0.85 = 85% quality, good balance)
                        javax.imageio.ImageWriteParam jpegParams = jpegWriter.getDefaultWriteParam();
                        jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                        jpegParams.setCompressionQuality(0.85f);

                        jpegWriter.write(null, new javax.imageio.IIOImage(optimizedImage, null, null), jpegParams);
                        jpegWriter.dispose();
                        ios.close();

                        // Create PDF image directly from the compressed file
                        // This preserves the JPEG compression and is much more memory efficient
                        img = PDImageXObject.createFromFile(tempJpeg.getAbsolutePath(), doc);

                    } catch (Exception compressionError) {
                        System.err.println("JPEG compression failed, using optimized image directly: "
                                + compressionError.getMessage());
                        compressionError.printStackTrace();
                        // Fallback: use optimized image logic via LosslessFactory (heavier but safe
                        // fallback)
                        img = LosslessFactory.createFromImage(doc, optimizedImage);
                    } finally {
                        if (tempJpeg != null && tempJpeg.exists()) {
                            try {
                                tempJpeg.delete();
                            } catch (Exception e) {
                                // Ignore cleanup errors
                            }
                        }
                    }

                    PDPageContentStream cs = new PDPageContentStream(doc, page);

                    // Calculate image dimensions to fit page while maintaining aspect ratio
                    float pageWidth = page.getMediaBox().getWidth() - 40; // 20px margin on each side
                    float pageHeight = page.getMediaBox().getHeight() - 40; // 20px margin on each side

                    float finalImgWidth = img.getWidth();
                    float finalImgHeight = img.getHeight();

                    // Calculate scaling to fit within page bounds
                    float scaleX = pageWidth / finalImgWidth;
                    float scaleY = pageHeight / finalImgHeight;
                    float scale = Math.min(scaleX, scaleY); // Use smaller scale to fit both dimensions

                    float scaledWidth = finalImgWidth * scale;
                    float scaledHeight = finalImgHeight * scale;

                    // Center the image on the page
                    float x = (pageWidth - scaledWidth) / 2 + 20;
                    float y = (pageHeight - scaledHeight) / 2 + 20;

                    cs.drawImage(img, x, y, scaledWidth, scaledHeight);
                    cs.close();
                } catch (Exception e) {
                    // If one image fails, continue with others
                    // Remove the page we just added if image loading failed
                    if (doc.getNumberOfPages() > 0) {
                        doc.removePage(doc.getNumberOfPages() - 1);
                    }
                    System.err.println("Failed to add image: " + imgPath + " - " + e.getMessage());
                    e.printStackTrace(); // Print stack trace for debugging
                }
            }

            // Check if we have at least one valid page
            if (doc.getNumberOfPages() == 0) {
                throw new Exception("No valid images could be processed");
            }

            doc.save(output);
        } finally {
            doc.close();
        }

        return output;
    }

    /**
     * Compresses and optimizes a BufferedImage for PDF inclusion
     * Reduces image quality slightly to significantly reduce file size
     */
    /**
     * Compresses, scales, and rotates the image for PDF inclusion.
     * 1. Scale down if too large (memory optimization).
     * 2. Rotate pixels based on EXIF orientation (correct display).
     * 3. Convert to RGB (remove alpha).
     */
    private static BufferedImage compressAndOrientImage(BufferedImage original, int orientation) {
        int width = original.getWidth();
        int height = original.getHeight();

        // 1. Calculate Scaling (Subsampling might have already done most of the work)
        // We still check just in case subsampling was conservative
        int maxDimension = 2048;
        int targetWidth = width;
        int targetHeight = height;

        if (width > maxDimension || height > maxDimension) {
            double scale = (width > height) ? (double) maxDimension / width : (double) maxDimension / height;
            targetWidth = (int) (width * scale);
            targetHeight = (int) (height * scale);
        }

        // 2. Determine Output Dimensions (Swapped if 90/270 rotation)
        boolean isRotated90 = (orientation == 5 || orientation == 6 || orientation == 7 || orientation == 8);
        int outputWidth = isRotated90 ? targetHeight : targetWidth;
        int outputHeight = isRotated90 ? targetWidth : targetHeight;

        BufferedImage optimized = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = optimized.createGraphics();

        try {
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            // Apply Rotation and Scaling via Transform
            AffineTransform t = new AffineTransform();

            // Rotation Logic
            switch (orientation) {
                case 1: // Normal
                    System.out.println("DEBUG: Use Normal Orientation");
                    break;
                case 3: // 180
                    System.out.println("DEBUG: Rotating 180");
                    t.translate(outputWidth, outputHeight);
                    t.rotate(Math.PI);
                    break;
                case 6: // 90 CW
                    System.out.println("DEBUG: Rotating 90 CW");
                    t.translate(outputWidth, 0);
                    t.rotate(Math.PI / 2);
                    break;
                case 8: // 270 CW (90 CCW)
                    System.out.println("DEBUG: Rotating 270 CW");
                    t.translate(0, outputHeight);
                    t.rotate(-Math.PI / 2);
                    break;
                default:
                    System.out.println("DEBUG: Other Orientation " + orientation);
            }

            // Scale to target
            double scaleX = (double) targetWidth / width;
            double scaleY = (double) targetHeight / height;
            t.scale(scaleX, scaleY);

            g.drawImage(original, t, null);
        } finally {
            g.dispose();
        }

        return optimized;
    }

    /**
     * Reads an image with subsampling to memory usage.
     * Reads every Nth pixel to ensure the loaded image is approx targetMaxSize.
     */
    private static BufferedImage readSubsampledImage(File imageFile, int targetMaxSize) throws Exception {
        try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile)) {
            java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("No image reader found for: " + imageFile.getName());
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                int subsampling = 1;
                if (width > targetMaxSize || height > targetMaxSize) {
                    int wSub = width / targetMaxSize;
                    int hSub = height / targetMaxSize;

                    // Use the larger factor to ensure we stay under limit
                    subsampling = Math.max(wSub, hSub);
                    if (subsampling < 1)
                        subsampling = 1;
                }

                javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
                if (subsampling > 1) {
                    param.setSourceSubsampling(subsampling, subsampling, 0, 0);
                }

                return reader.read(0, param);
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * Gets EXIF orientation by reading raw JPEG bytes directly.
     * This bypasses ImageIO's unreliable metadata reader.
     * Returns orientation value (1-8) or 1 if not found/error
     */
    private static int getExifOrientation(File imageFile) {
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imageFile, "r")) {
            // Check JPEG signature
            if (raf.readUnsignedByte() != 0xFF || raf.readUnsignedByte() != 0xD8) {
                return 1; // Not a JPEG
            }

            // Read JPEG markers to find APP1 (EXIF)
            while (true) {
                int marker = raf.readUnsignedByte();
                if (marker != 0xFF)
                    break;

                int markerType = raf.readUnsignedByte();
                if (markerType == 0xD8 || markerType == 0xD9)
                    continue; // SOI/EOI have no length

                int length = raf.readUnsignedShort() - 2;

                if (markerType == 0xE1) { // APP1 - EXIF marker
                    byte[] exifData = new byte[length];
                    raf.readFully(exifData);

                    // Check for EXIF header
                    if (length < 6 || exifData[0] != 'E' || exifData[1] != 'x' ||
                            exifData[2] != 'i' || exifData[3] != 'f') {
                        continue;
                    }

                    return parseExifOrientation(exifData);
                }

                // Skip this marker's data
                raf.skipBytes(length);

                // Stop at Start of Scan
                if (markerType == 0xDA)
                    break;
            }
        } catch (Exception e) {
            System.err.println("EXIF read error: " + e.getMessage());
        }
        return 1; // Default orientation
    }

    private static int parseExifOrientation(byte[] exifData) {
        try {
            // Skip "Exif\0\0"
            int offset = 6;

            // Check byte order
            boolean bigEndian = (exifData[offset] == 'M' && exifData[offset + 1] == 'M');
            offset += 2;

            // Skip TIFF magic number (0x002A)
            offset += 2;

            // Get offset to IFD0
            int ifd0Offset = offset + readInt(exifData, offset, bigEndian, 4);

            // Read number of directory entries
            int numEntries = readInt(exifData, ifd0Offset, bigEndian, 2);
            ifd0Offset += 2;

            // Search for Orientation tag (0x0112)
            for (int i = 0; i < numEntries; i++) {
                int entryOffset = ifd0Offset + (i * 12);
                int tag = readInt(exifData, entryOffset, bigEndian, 2);

                if (tag == 0x0112) { // Orientation tag
                    int orientation = readInt(exifData, entryOffset + 8, bigEndian, 2);
                    System.out.println("DEBUG: Found EXIF Orientation tag: " + orientation);
                    return orientation;
                }
            }
        } catch (Exception e) {
            System.err.println("EXIF parse error: " + e.getMessage());
        }
        return 1;
    }

    private static int readInt(byte[] data, int offset, boolean bigEndian, int length) {
        if (length == 2) {
            if (bigEndian) {
                return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
            } else {
                return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
            }
        } else if (length == 4) {
            if (bigEndian) {
                return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16) |
                        ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
            } else {
                return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) |
                        ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24);
            }
        }
        return 0;
    }
}
