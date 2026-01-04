# Image Utility - Project Brain

## 📋 Project Overview

**Project Name:** Image to PDF Converter (image-utility)  
**Type:** Java Web Application  
**Primary Function:** Convert single or multiple images to PDF with proper orientation handling  
**Framework:** Java Servlets with JSP  
**Server:** Apache Tomcat 9.0.113  

### Purpose

This web application provides a user-friendly interface for converting images to PDF format. It handles:
- Multiple image formats (JPEG, PNG, WebP)
- EXIF orientation metadata for correct image rotation
- Batch conversion of multiple images into a single PDF
- Automatic image optimization and compression
- File size management with configurable limits

### Key Features

- ✅ **Multi-format Support**: JPEG, PNG, WebP
- ✅ **Batch Processing**: Convert multiple images at once
- ✅ **EXIF Handling**: Automatic orientation correction based on EXIF data
- ✅ **Image Optimization**: Compression and scaling for memory efficiency
- ✅ **Modern UI**: Tailwind CSS-based responsive interface
- ✅ **File Size Limits**: 50 MB per file, 300 MB total request
- ✅ **Secure Downloads**: Protected file download servlet
- ✅ **Error Handling**: Comprehensive error messages and validation

## 🎯 Quick Start

### Prerequisites
- Java 11 or higher
- Apache Tomcat 9.0.113
- Windows OS (batch script provided)

### Running the Application

```bash
# From project root
.\deploy_and_run.bat
```

This will:
1. Clean previous deployment
2. Compile Java sources
3. Deploy to Tomcat webapps
4. Start Tomcat server

### Accessing the Application

- **URL**: `http://localhost:8080/project-image-utility-tool/`
- **Main Page**: `index.jsp` - Upload interface
- **Result Page**: `result.jsp` - Download converted PDF

## 📁 Project Structure

```
image-utility/
├── brain/                          # Project documentation (this folder)
├── build/                          # Compiled classes
│   └── classes/
│       ├── controller/            # Servlet classes
│       └── util/                  # Utility classes
├── src/
│   └── main/
│       ├── java/
│       │   ├── controller/        # HTTP request handlers
│       │   │   ├── ImageServlet.java      # Image upload & conversion
│       │   │   └── DownloadServlet.java   # File download handler
│       │   └── util/
│       │       └── ImageUtils.java        # Core conversion logic
│       └── webapp/                # Web resources
│           ├── WEB-INF/
│           │   ├── lib/          # JAR dependencies
│           │   │   ├── pdfbox-2.0.30.jar
│           │   │   ├── commons-logging-1.2.jar
│           │   │   └── webp-imageio-0.1.6.jar
│           │   └── uploads/      # Temporary file storage
│           ├── index.jsp         # Main upload page
│           └── result.jsp        # Success/download page
├── test/
│   └── TestExif.java             # EXIF parser test utility
├── deploy_and_run.bat            # Deployment script
├── download-webp-library.ps1     # WebP library downloader
└── WEBP_LIBRARY_SETUP.md         # WebP setup instructions
```

## 🏗️ Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 11+ |
| Web Framework | Java Servlets | 4.0 |
| View Layer | JSP | 2.3 |
| Server | Apache Tomcat | 9.0.113 |
| PDF Library | Apache PDFBox | 2.0.30 |
| Image I/O | WebP ImageIO | 0.1.6 |
| Frontend | Tailwind CSS CDN | Latest |
| Font | Google Fonts (Inter) | Latest |

### Application Flow

```
User Upload (index.jsp)
    ↓
ImageServlet.doPost()
    ↓
├─ Validate files
├─ Save to uploads/
├─ Call ImageUtils.imagesToPdf()
│   ├─ Read images with subsampling
│   ├─ Read EXIF orientation
│   ├─ Rotate & optimize images
│   ├─ Create PDF pages
│   └─ Save PDF
├─ Clean up temp images
└─ Forward to result.jsp
    ↓
User Downloads (DownloadServlet)
```

## 🔧 Configuration

### File Size Limits

**ImageServlet.java** (lines 13-17):
```java
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 5,    // 5 MB in-memory threshold
    maxFileSize = 1024 * 1024 * 50,         // 50 MB per file
    maxRequestSize = 1024 * 1024 * 300      // 300 MB total
)
```

### Tomcat Configuration

**deploy_and_run.bat** (lines 2-4):
```bat
set "TOMCAT_HOME=C:\apache-tomcat-9.0.113\apache-tomcat-9.0.113"
set "APP_NAME=project-image-utility-tool"
```

### Image Optimization Settings

**ImageUtils.java** (line 39, 215):
- **Max Dimension**: 2048 pixels (subsampled on load)
- **JPEG Quality**: 85% compression
- **Page Margins**: 20px on all sides

## 📚 Quick Reference

### Main Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/ImageServlet` | POST | Upload & convert images |
| `/download?file={filename}` | GET | Download generated PDF |

### Key Classes

| Class | Purpose |
|-------|---------|
| `ImageServlet` | Handle image uploads, orchestrate conversion |
| `DownloadServlet` | Serve generated PDFs securely |
| `ImageUtils` | Core PDF conversion & EXIF handling |
| `TestExif` | Standalone EXIF testing utility |

### Dependencies

| Library | Purpose |
|---------|---------|
| **pdfbox-2.0.30.jar** | PDF generation and manipulation |
| **commons-logging-1.2.jar** | Logging (PDFBox dependency) |
| **webp-imageio-0.1.6.jar** | WebP format support |

## 📖 Documentation Index

For detailed information, see:

1. **[Architecture Documentation](./architecture.md)** - System design and data flow
2. **[API Documentation](./api-documentation.md)** - Servlet endpoints and methods
3. **[Java Classes Documentation](./java-classes.md)** - Detailed class analysis
4. **[Frontend Documentation](./frontend.md)** - JSP pages and UI components
5. **[Deployment Guide](./deployment.md)** - Build and deployment instructions
6. **[Dependencies](./dependencies.md)** - External libraries and their usage
7. **[Development Guide](./development-guide.md)** - How to add new features

## 🔍 Common Operations

### Adding New Image Formats

1. Add ImageIO library to `WEB-INF/lib/`
2. Update `DownloadServlet` content type mapping (lines 38-52)
3. Update `index.jsp` file validation if needed

### Changing PDF Page Size

Edit `ImageUtils.java` (lines 69-75):
```java
// Portrait: A4 (595 x 842 points)
// Landscape: A4 rotated (842 x 595 points)
```

### Modifying Compression Quality

Edit `ImageUtils.java` (line 125):
```java
jpegParams.setCompressionQuality(0.85f); // 85% quality
```

## 🐛 Troubleshooting

See individual documentation files for detailed troubleshooting:
- WebP issues → `WEBP_LIBRARY_SETUP.md`
- EXIF debugging → Use `TestExif.java`
- Deployment errors → `deployment.md`

---

**Last Updated:** 2025-12-25  
**Version:** 1.0  
**Maintainer:** Project Team
