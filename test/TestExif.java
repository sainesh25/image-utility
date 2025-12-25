import java.io.*;

public class TestExif {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java TestExif <image-file>");
            return;
        }

        File imageFile = new File(args[0]);
        if (!imageFile.exists()) {
            System.out.println("File not found: " + args[0]);
            return;
        }

        System.out.println("Testing EXIF parser on: " + imageFile.getAbsolutePath());
        System.out.println("File size: " + imageFile.length() + " bytes");

        int orientation = getExifOrientation(imageFile);
        System.out.println("==> EXIF Orientation: " + orientation);
    }

    private static int getExifOrientation(File imageFile) {
        try (RandomAccessFile raf = new RandomAccessFile(imageFile, "r")) {
            System.out.println("[1] Checking JPEG signature...");
            int b1 = raf.readUnsignedByte();
            int b2 = raf.readUnsignedByte();
            System.out.println("    First two bytes: 0x" + Integer.toHexString(b1) + " 0x" + Integer.toHexString(b2));

            if (b1 != 0xFF || b2 != 0xD8) {
                System.out.println("    NOT a JPEG file!");
                return 1;
            }
            System.out.println("    Valid JPEG signature");

            System.out.println("[2] Scanning for APP1 marker...");
            while (true) {
                int marker = raf.readUnsignedByte();
                if (marker != 0xFF) {
                    System.out.println("    Invalid marker byte: 0x" + Integer.toHexString(marker));
                    break;
                }

                int markerType = raf.readUnsignedByte();
                System.out.println("    Found marker: 0xFF" + Integer.toHexString(markerType).toUpperCase());

                if (markerType == 0xD8 || markerType == 0xD9)
                    continue;

                int length = raf.readUnsignedShort() - 2;
                System.out.println("    Marker length: " + length);

                if (markerType == 0xE1) {
                    System.out.println("[3] Found APP1 (EXIF) marker!");
                    byte[] exifData = new byte[length];
                    raf.readFully(exifData);

                    System.out.println("    First 6 bytes: " +
                            (char) exifData[0] + (char) exifData[1] + (char) exifData[2] +
                            (char) exifData[3] + (char) exifData[4] + (char) exifData[5]);

                    if (length < 6 || exifData[0] != 'E' || exifData[1] != 'x' ||
                            exifData[2] != 'i' || exifData[3] != 'f') {
                        System.out.println("    Not EXIF data, continuing...");
                        continue;
                    }

                    return parseExifOrientation(exifData);
                }

                raf.skipBytes(length);
                if (markerType == 0xDA) {
                    System.out.println("    Reached Start of Scan, stopping");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 1;
    }

    private static int parseExifOrientation(byte[] exifData) {
        try {
            System.out.println("[4] Parsing EXIF structure...");
            int offset = 6;

            char b1 = (char) exifData[offset];
            char b2 = (char) exifData[offset + 1];
            System.out.println("    Byte order: " + b1 + b2);

            boolean bigEndian = (b1 == 'M' && b2 == 'M');
            System.out.println("    Big endian: " + bigEndian);
            offset += 2;

            offset += 2;

            int ifd0Offset = offset + readInt(exifData, offset, bigEndian, 4);
            System.out.println("    IFD0 offset: " + ifd0Offset);

            int numEntries = readInt(exifData, ifd0Offset, bigEndian, 2);
            System.out.println("    Number of IFD entries: " + numEntries);
            ifd0Offset += 2;

            System.out.println("[5] Searching for Orientation tag (0x0112)...");
            for (int i = 0; i < numEntries; i++) {
                int entryOffset = ifd0Offset + (i * 12);
                int tag = readInt(exifData, entryOffset, bigEndian, 2);

                if (tag == 0x0112) {
                    int orientation = readInt(exifData, entryOffset + 8, bigEndian, 2);
                    System.out.println("    FOUND! Orientation = " + orientation);
                    return orientation;
                }
            }
            System.out.println("    Orientation tag not found");
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            e.printStackTrace();
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
