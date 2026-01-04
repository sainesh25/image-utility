# Java Classes Documentation

## Overview

This document provides an in-depth analysis of all Java classes in the image-utility project.

## Table of Contents

1. [Controller Package](#controller-package)
   - [ImageServlet.java](#imageservletjava)
   - [DownloadServlet.java](#downloadservletjava)
2. [Util Package](#util-package)
   - [ImageUtils.java](#imageutilsjava)
3. [Test Package](#test-package)
   - [TestExif.java](#testexifjava)

---

## Controller Package

Located in: `src/main/java/controller/`

### ImageServlet.java

**Full Path:** `src/main/java/controller/ImageServlet.java`  
**Lines:** 110  
**Purpose:** Main servlet for handling image uploads and orchestrating PDF conversion  
**URL Mapping:** `/ImageServlet`

#### Class Declaration

```java
@SuppressWarnings("serial")
@WebServlet("/ImageServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 5,    // 5 MB
    maxFileSize = 1024 * 1024 * 50,         // 50 MB per file
    maxRequestSize = 1024 * 1024 * 300      // 300 MB total
)
public class ImageServlet extends HttpServlet
```

#### Annotations

- **@WebServlet**: Maps servlet to `/ImageServlet` URL
- **@MultipartConfig**: Enables multipart file upload with size limits
  - `fileSizeThreshold`: 5 MB (files larger than this are written to disk)
  - `maxFileSize`: 50 MB maximum per individual file
  - `maxRequestSize`: 300 MB total for all files in one request

#### Methods

##### doPost(HttpServletRequest req, HttpServletResponse res)

**Purpose:** Handle POST requests with file uploads

**Parameters:**
- `req` - HTTP request containing multipart form data
- `res` - HTTP response for redirects

**Process Flow:**

1. **File Collection** (lines 24-49)
   ```java
   Collection<Part> parts = req.getParts();
   List<String> imagePaths = new ArrayList<>();
   
   for (Part part : parts) {
       if ("image".equals(part.getName()) && part.getSize() > 0) {
           // Process each file
       }
   }
   ```

2. **Upload Directory Management** (lines 28-30)
   ```java
   String uploadPath = getServletContext().getRealPath("/") + "uploads/";
   File uploadDir = new File(uploadPath);
   if (!uploadDir.exists()) uploadDir.mkdirs();
   ```

3. **File Validation** (lines 39-43)
   - Check content type starts with `"image/"`
   - Skip non-image files

4. **File Saving** (lines 36-47)
   - Generate unique filename: `timestamp_originalName`
   - Save to uploads directory
   - Add path to processing list

5. **Empty Upload Check** (lines 52-55)
   - Redirect with error if no valid images

6. **PDF Conversion** (lines 57-76)
   ```java
   try {
       String[] imgPathsArray = imagePaths.toArray(new String[0]);
       resultFile = ImageUtils.imagesToPdf(
           imgPathsArray,
           uploadPath + "output_" + System.currentTimeMillis() + ".pdf"
       );
   } catch (Exception e) {
       // Cleanup and error handling
   }
   ```

7. **Temporary File Cleanup** (lines 78-85)
   - Delete uploaded images after PDF creation
   - Ignore cleanup errors

8. **Success Handling** (lines 88-90)
   ```java
   String resultFileName = new File(resultFile).getName();
   req.setAttribute("result", resultFileName);
   req.getRequestDispatcher("result.jsp").forward(req, res);
   ```

**Exception Handling:**

1. **IllegalStateException** (lines 92-104)
   - Caught when file size limits are exceeded
   - Extracts cause message for user feedback
   - Redirects to index with error message

2. **General Exception** (lines 105-107)
   - Catches any other errors
   - Redirects with generic error message

**Error Redirection Pattern:**
```java
res.sendRedirect("index.jsp?error=" + 
    URLEncoder.encode(errorMessage, "UTF-8"));
```

#### Key Features

- ✅ Multi-file upload support
- ✅ Content type validation
- ✅ Size limit enforcement
- ✅ Unique filename generation
- ✅ Automatic cleanup
- ✅ Comprehensive error handling
- ✅ URL-encoded error messages

---

### DownloadServlet.java

**Full Path:** `src/main/java/controller/DownloadServlet.java`  
**Lines:** 72  
**Purpose:** Secure file download handler  
**URL Mapping:** `/download`

#### Class Declaration

```java
@SuppressWarnings("serial")
@WebServlet("/download")
public class DownloadServlet extends HttpServlet
```

#### Methods

##### doGet(HttpServletRequest req, HttpServletResponse res)

**Purpose:** Stream files to client with security checks

**Parameters:**
- `req` - GET request with `file` parameter
- `res` - Response for streaming

**Process Flow:**

1. **Parameter Validation** (lines 15-19)
   ```java
   String fileName = req.getParameter("file");
   if (fileName == null || fileName.isEmpty()) {
       res.sendError(SC_BAD_REQUEST, "File parameter is required");
   }
   ```

2. **Security: Path Traversal Prevention** (lines 21-25)
   ```java
   if (fileName.contains("..") || 
       fileName.contains("/") || 
       fileName.contains("\\")) {
       res.sendError(SC_FORBIDDEN, "Invalid file path");
   }
   ```
   - Prevents directory traversal attacks
   - Blocks `../`, `/`, `\` characters

3. **File Existence Check** (lines 27-33)
   ```java
   String uploadPath = getServletContext().getRealPath("/") + "uploads/";
   File file = new File(uploadPath + fileName);
   
   if (!file.exists() || !file.isFile()) {
       res.sendError(SC_NOT_FOUND, "File not found");
   }
   ```

4. **Content Type Determination** (lines 35-52)
   ```java
   String contentType = "application/octet-stream";
   String fileExtension = fileName.substring(
       fileName.lastIndexOf('.') + 1
   ).toLowerCase();
   
   switch (fileExtension) {
       case "jpg":
       case "jpeg":
           contentType = "image/jpeg";
           break;
       case "png":
           contentType = "image/png";
           break;
       case "webp":
           contentType = "image/webp";
           break;
       case "pdf":
           contentType = "application/pdf";
           break;
   }
   ```

5. **Response Headers** (lines 54-57)
   ```java
   res.setContentType(contentType);
   res.setHeader("Content-Disposition", 
       "attachment; filename=\"" + fileName + "\"");
   res.setContentLengthLong(file.length());
   ```

6. **File Streaming** (lines 59-68)
   ```java
   try (InputStream in = new FileInputStream(file);
        OutputStream out = res.getOutputStream()) {
       
       byte[] buffer = new byte[4096];
       int bytesRead;
       while ((bytesRead = in.read(buffer)) != -1) {
           out.write(buffer, 0, bytesRead);
       }
   }
   ```

#### Key Features

- ✅ Path traversal protection
- ✅ File existence validation
- ✅ Content type detection
- ✅ Efficient streaming (4KB buffer)
- ✅ Proper resource management (try-with-resources)
- ✅ Multiple format support

#### Supported File Types

| Extension | MIME Type |
|-----------|-----------|
| jpg, jpeg | image/jpeg |
| png | image/png |
| webp | image/webp |
| pdf | application/pdf |
| others | application/octet-stream |

---

## Util Package

Located in: `src/main/java/util/`

### ImageUtils.java

**Full Path:** `src/main/java/util/ImageUtils.java`  
**Lines:** 425  
**Purpose:** Core image processing and PDF conversion logic  
**Type:** Utility class (all static methods)

#### Imports

```java
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
```

#### Public Methods

##### imageToPdf(String imgPath, String output)

**Lines:** 17-20  
**Purpose:** Convert single image to PDF (backward compatibility wrapper)

```java
public static String imageToPdf(String imgPath, String output) throws Exception {
    return imagesToPdf(new String[] { imgPath }, output);
}
```

##### imagesToPdf(String[] imgPaths, String output)

**Lines:** 22-197  
**Purpose:** Main conversion method - convert multiple images to single PDF

**Parameters:**
- `imgPaths` - Array of image file paths
- `output` - Output PDF file path

**Returns:** String - Path to generated PDF

**Exceptions:** 
- IllegalArgumentException if imgPaths is null or empty
- Exception for any processing errors

**Process Flow:**

1. **Validation** (lines 23-25)
   ```java
   if (imgPaths == null || imgPaths.length == 0) {
       throw new IllegalArgumentException(
           "At least one image path is required"
       );
   }
   ```

2. **PDF Document Creation** (line 27)
   ```java
   PDDocument doc = new PDDocument();
   ```

3. **For Each Image** (lines 31-184):

   **a. Read with Subsampling** (line 39)
   ```java
   BufferedImage bufferedImage = readSubsampledImage(imageFile, 2048);
   ```

   **b. Get Dimensions** (lines 42-43)
   ```java
   int imgWidth = bufferedImage.getWidth();
   int imgHeight = bufferedImage.getHeight();
   ```

   **c. Read EXIF Orientation** (line 46)
   ```java
   int orientation = getExifOrientation(imageFile);
   ```

   **d. Auto-rotation Fallback** (lines 54-62)
   ```java
   // For landscape images without EXIF with phone aspect ratio
   if (orientation == 1 && imgWidth > imgHeight) {
       float aspectRatio = (float) imgWidth / imgHeight;
       if (aspectRatio >= 1.3f && aspectRatio <= 1.4f) {
           orientation = 6; // Rotate 90° CW
       }
   }
   ```

   **e. Determine Page Orientation** (lines 64-75)
   ```java
   boolean isPortrait = imgHeight > imgWidth;
   PDPage page;
   if (isPortrait) {
       page = new PDPage(new PDRectangle(595, 842)); // A4 Portrait
   } else {
       page = new PDPage(new PDRectangle(842, 595)); // A4 Landscape
   }
   ```

   **f. Process Image** (line 81)
   ```java
   BufferedImage optimizedImage = compressAndOrientImage(
       bufferedImage, orientation
   );
   ```

   **g. Adjust Page Orientation** (lines 94-101)
   ```java
   boolean finalIsPortrait = 
       optimizedImage.getHeight() > optimizedImage.getWidth();
   if (finalIsPortrait) {
       page.setMediaBox(new PDRectangle(595, 842));
   } else {
       page.setMediaBox(new PDRectangle(842, 595));
   }
   ```

   **h. JPEG Compression** (lines 107-149)
   ```java
   // Create temp JPEG with 85% quality
   tempJpeg = File.createTempFile("pdf_temp_", ".jpg");
   ImageWriter jpegWriter = ...;
   ImageWriteParam jpegParams = jpegWriter.getDefaultWriteParam();
   jpegParams.setCompressionMode(MODE_EXPLICIT);
   jpegParams.setCompressionQuality(0.85f);
   jpegWriter.write(...);
   
   // Create PDF image from compressed JPEG
   img = PDImageXObject.createFromFile(
       tempJpeg.getAbsolutePath(), doc
   );
   ```

   **i. Add to PDF Page** (lines 152-174)
   ```java
   PDPageContentStream cs = new PDPageContentStream(doc, page);
   
   // Calculate scaling to fit page (with 20px margins)
   float pageWidth = page.getMediaBox().getWidth() - 40;
   float pageHeight = page.getMediaBox().getHeight() - 40;
   float scale = Math.min(
       pageWidth / finalImgWidth,
       pageHeight / finalImgHeight
   );
   
   // Center image on page
   float x = (pageWidth - scaledWidth) / 2 + 20;
   float y = (pageHeight - scaledHeight) / 2 + 20;
   
   cs.drawImage(img, x, y, scaledWidth, scaledHeight);
   cs.close();
   ```

4. **Validation** (lines 186-189)
   ```java
   if (doc.getNumberOfPages() == 0) {
       throw new Exception("No valid images could be processed");
   }
   ```

5. **Save PDF** (line 191)
   ```java
   doc.save(output);
   ```

6. **Cleanup** (line 193)
   ```java
   doc.close();
   ```

#### Private Methods

##### compressAndOrientImage(BufferedImage original, int orientation)

**Lines:** 209-279  
**Purpose:** Scale, rotate, and optimize image for PDF

**Parameters:**
- `original` - Source image
- `orientation` - EXIF orientation value (1-8)

**Returns:** BufferedImage - Optimized and rotated image

**Process:**

1. **Calculate Scaling** (lines 210-223)
   ```java
   int maxDimension = 2048;
   int targetWidth = width;
   int targetHeight = height;
   
   if (width > maxDimension || height > maxDimension) {
       double scale = (width > height) 
           ? (double) maxDimension / width 
           : (double) maxDimension / height;
       targetWidth = (int) (width * scale);
       targetHeight = (int) (height * scale);
   }
   ```

2. **Determine Output Dimensions** (lines 226-228)
   ```java
   boolean isRotated90 = (orientation == 5 || orientation == 6 || 
                          orientation == 7 || orientation == 8);
   int outputWidth = isRotated90 ? targetHeight : targetWidth;
   int outputHeight = isRotated90 ? targetWidth : targetHeight;
   ```

3. **Create Output Image** (line 230)
   ```java
   BufferedImage optimized = new BufferedImage(
       outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB
   );
   ```

4. **Set Rendering Hints** (lines 234-239)
   ```java
   g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
   g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
   g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
   ```

5. **Apply Rotation** (lines 244-266)
   ```java
   AffineTransform t = new AffineTransform();
   
   switch (orientation) {
       case 1:  // Normal
           break;
       case 3:  // 180°
           t.translate(outputWidth, outputHeight);
           t.rotate(Math.PI);
           break;
       case 6:  // 90° CW
           t.translate(outputWidth, 0);
           t.rotate(Math.PI / 2);
           break;
       case 8:  // 270° CW
           t.translate(0, outputHeight);
           t.rotate(-Math.PI / 2);
           break;
   }
   ```

6. **Apply Scaling** (lines 269-271)
   ```java
   double scaleX = (double) targetWidth / width;
   double scaleY = (double) targetHeight / height;
   t.scale(scaleX, scaleY);
   ```

7. **Draw Image** (line 273)
   ```java
   g.drawImage(original, t, null);
   ```

##### readSubsampledImage(File imageFile, int targetMaxSize)

**Lines:** 285-318  
**Purpose:** Load image with subsampling to reduce memory usage

**Parameters:**
- `imageFile` - Image file to read
- `targetMaxSize` - Target maximum dimension (e.g., 2048)

**Returns:** BufferedImage - Subsampled image

**Process:**

1. **Get Image Reader** (lines 286-291)
   ```java
   ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
   Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
   if (!readers.hasNext()) {
       throw new IllegalArgumentException("No image reader found");
   }
   ImageReader reader = readers.next();
   ```

2. **Read Dimensions** (lines 293-295)
   ```java
   reader.setInput(iis);
   int width = reader.getWidth(0);
   int height = reader.getHeight(0);
   ```

3. **Calculate Subsampling Factor** (lines 297-306)
   ```java
   int subsampling = 1;
   if (width > targetMaxSize || height > targetMaxSize) {
       int wSub = width / targetMaxSize;
       int hSub = height / targetMaxSize;
       subsampling = Math.max(wSub, hSub);
       if (subsampling < 1) subsampling = 1;
   }
   ```

4. **Apply Subsampling** (lines 308-313)
   ```java
   ImageReadParam param = reader.getDefaultReadParam();
   if (subsampling > 1) {
       param.setSourceSubsampling(subsampling, subsampling, 0, 0);
   }
   return reader.read(0, param);
   ```

##### getExifOrientation(File imageFile)

**Lines:** 325-368  
**Purpose:** Read EXIF orientation from JPEG file by parsing raw bytes

**Parameters:**
- `imageFile` - JPEG image file

**Returns:** int - Orientation value (1-8), or 1 if not found

**Process:**

1. **Verify JPEG Signature** (lines 327-330)
   ```java
   if (raf.readUnsignedByte() != 0xFF || 
       raf.readUnsignedByte() != 0xD8) {
       return 1; // Not a JPEG
   }
   ```

2. **Find APP1 Marker** (lines 333-363)
   ```java
   while (true) {
       int marker = raf.readUnsignedByte();
       if (marker != 0xFF) break;
       
       int markerType = raf.readUnsignedByte();
       int length = raf.readUnsignedShort() - 2;
       
       if (markerType == 0xE1) { // APP1 - EXIF
           byte[] exifData = new byte[length];
           raf.readFully(exifData);
           
           // Verify EXIF header
           if (exifData[0] == 'E' && exifData[1] == 'x' &&
               exifData[2] == 'i' && exifData[3] == 'f') {
               return parseExifOrientation(exifData);
           }
       }
       
       raf.skipBytes(length);
       if (markerType == 0xDA) break; // Start of Scan
   }
   ```

##### parseExifOrientation(byte[] exifData)

**Lines:** 370-404  
**Purpose:** Parse EXIF byte array to extract orientation tag

**Parameters:**
- `exifData` - Raw EXIF data bytes

**Returns:** int - Orientation value (1-8), or 1 if not found

**Process:**

1. **Skip EXIF Header** (line 373)
   ```java
   int offset = 6; // Skip "Exif\0\0"
   ```

2. **Determine Byte Order** (line 376)
   ```java
   boolean bigEndian = (exifData[offset] == 'M' && 
                        exifData[offset + 1] == 'M');
   ```

3. **Get IFD0 Offset** (line 383)
   ```java
   int ifd0Offset = offset + readInt(exifData, offset, bigEndian, 4);
   ```

4. **Read Number of Entries** (line 386)
   ```java
   int numEntries = readInt(exifData, ifd0Offset, bigEndian, 2);
   ```

5. **Search for Orientation Tag (0x0112)** (lines 390-398)
   ```java
   for (int i = 0; i < numEntries; i++) {
       int entryOffset = ifd0Offset + (i * 12);
       int tag = readInt(exifData, entryOffset, bigEndian, 2);
       
       if (tag == 0x0112) { // Orientation tag
           int orientation = readInt(
               exifData, entryOffset + 8, bigEndian, 2
           );
           return orientation;
       }
   }
   ```

##### readInt(byte[] data, int offset, boolean bigEndian, int length)

**Lines:** 406-423  
**Purpose:** Read multi-byte integer from byte array

**Parameters:**
- `data` - Byte array
- `offset` - Starting position
- `bigEndian` - Byte order
- `length` - 2 or 4 bytes

**Returns:** int - Parsed integer value

**Implementation:**

```java
if (length == 2) {
    if (bigEndian) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    } else {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }
} else if (length == 4) {
    if (bigEndian) {
        return ((data[offset] & 0xFF) << 24) | 
               ((data[offset + 1] & 0xFF) << 16) |
               ((data[offset + 2] & 0xFF) << 8) | 
               (data[offset + 3] & 0xFF);
    } else {
        return (data[offset] & 0xFF) | 
               ((data[offset + 1] & 0xFF) << 8) |
               ((data[offset + 2] & 0xFF) << 16) | 
               ((data[offset + 3] & 0xFF) << 24);
    }
}
```

---

## Test Package

Located in: `test/`

### TestExif.java

**Full Path:** `test/TestExif.java`  
**Lines:** 144  
**Purpose:** Standalone utility for testing EXIF orientation parsing  
**Usage:** `java TestExif <image-file>`

#### Purpose

Diagnostic tool to:
- Verify EXIF data presence
- Debug orientation detection
- Test parsing logic independently

#### Key Features

- Verbose output showing each parsing step
- JPEG signature verification
- Marker scanning with details
- EXIF structure parsing
- Orientation tag extraction

#### Output Example

```
Testing EXIF parser on: /path/to/image.jpg
File size: 1234567 bytes
[1] Checking JPEG signature...
    First two bytes: 0xff 0xd8
    Valid JPEG signature
[2] Scanning for APP1 marker...
    Found marker: 0xFFE1
    Marker length: 12345
[3] Found APP1 (EXIF) marker!
    First 6 bytes: Exif··
[4] Parsing EXIF structure...
    Byte order: MM
    Big endian: true
    IFD0 offset: 8
    Number of IFD entries: 12
[5] Searching for Orientation tag (0x0112)...
    FOUND! Orientation = 6
==> EXIF Orientation: 6
```

#### Methods

Identical implementations to `ImageUtils.java`:
- `getExifOrientation(File imageFile)`
- `parseExifOrientation(byte[] exifData)`
- `readInt(byte[] data, int offset, boolean bigEndian, int length)`

---

## Class Relationships

```
┌──────────────────┐
│   ImageServlet   │
│   (Controller)   │
└────────┬─────────┘
         │ uses
         ▼
┌──────────────────┐        ┌─────────────────┐
│   ImageUtils     │        │ DownloadServlet │
│  (Business Logic)│        │   (Controller)  │
└──────────────────┘        └─────────────────┘

         ⇅
   ┌──────────────┐
   │  Apache      │
   │  PDFBox      │
   └──────────────┘
```

---

**Next:** [API Documentation](./api-documentation.md)
