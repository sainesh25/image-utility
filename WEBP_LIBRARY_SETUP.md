# WebP Library Setup Instructions

## Overview
WebP support has been added to the Image Utility Tool. To enable WebP conversion, you need to add the WebP ImageIO library to your project.

## Option 1: Manual Download (Recommended)

1. **Download the library:**
   - Visit one of these sources:
     - GitHub: https://github.com/gotson/webp-imageio/releases
     - Or search for "webp-imageio-core" on Maven Central
   
2. **Download the JAR file:**
   - Look for `webp-imageio-core-0.4.4.jar` or latest version
   - Download the JAR file

3. **Place the JAR in your project:**
   - Copy the downloaded JAR file to: `src/main/webapp/WEB-INF/lib/`
   - The file should be alongside your other JAR files (commons-logging-1.2.jar, pdfbox-2.0.30.jar)

4. **Rebuild your project:**
   - Recompile your Java classes
   - Restart your web server/application server

## Option 2: Using Maven (if you have Maven installed)

If you have Maven installed, you can use this command from the project root:

```bash
mvn dependency:copy -Dartifact=com.github.gotson:webp-imageio-core:0.4.4 -DoutputDirectory=src/main/webapp/WEB-INF/lib
```

## Option 3: Using the PowerShell Script

Run the provided PowerShell script:

```powershell
powershell -ExecutionPolicy Bypass -File download-webp-library.ps1
```

Note: The script may fail due to network issues. In that case, use Option 1 (Manual Download).

## Verification

After adding the library:

1. Restart your application server
2. Try converting an image to WebP format
3. If conversion works, the library is properly installed

## Troubleshooting

- **"WebP format is not supported" error:**
  - Make sure the JAR file is in `src/main/webapp/WEB-INF/lib/`
  - Restart your application server
  - Check that the JAR file is not corrupted

- **ClassNotFoundException:**
  - Verify the JAR file is in the correct location
  - Ensure your build path includes the WEB-INF/lib directory
  - Rebuild your project

## Library Information

- **Library:** webp-imageio-core
- **Group ID:** com.github.gotson
- **Artifact ID:** webp-imageio-core
- **Version:** 0.4.4 (or latest)
- **License:** Apache License 2.0

