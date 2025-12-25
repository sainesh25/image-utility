# Image to PDF Converter

A web application that converts images to PDF format with automatic EXIF orientation handling and support for multiple image formats including WebP.

![Java](https://img.shields.io/badge/Java-11+-orange.svg)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## ✨ Features

- 🖼️ **Multi-format Support** - JPEG, PNG, WebP
- 📄 **Batch Conversion** - Convert multiple images to a single PDF
- 🔄 **EXIF Orientation** - Automatic rotation based on camera metadata
- 🎨 **Modern UI** - Clean, responsive interface with Tailwind CSS
- ⚡ **Fast Processing** - Optimized image compression and scaling
- 📦 **Memory Efficient** - Smart subsampling for large images
- ☁️ **Cloud Ready** - Deploy to Railway with one click

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- Apache Tomcat 9.0+ (for local deployment)
- Maven 3.6+ (optional, for building)

### Local Development

**Option 1: Using Batch Script (Windows)**

```bash
.\deploy_and_run.bat
```

**Option 2: Using Maven**

```bash
# Build
mvn clean package

# Deploy to Tomcat
copy target\image-utility.war path\to\tomcat\webapps\

# Start Tomcat
catalina.bat run
```

### Access the Application

Open your browser and navigate to:
```
http://localhost:8080/project-image-utility-tool/
```

## 📖 Usage

1. **Select Images** - Click "Choose Files" and select one or more images
2. **Upload** - Click "Convert to PDF"
3. **Download** - Download your generated PDF file

### Supported Formats

- JPEG / JPG
- PNG
- WebP

### File Size Limits

- Maximum per file: 50 MB
- Maximum total upload: 300 MB

## 🏗️ Architecture

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ ImageServlet│ ──► Upload & Validation
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ ImageUtils  │ ──► EXIF Reading, Rotation, PDF Generation
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  PDFBox     │ ──► PDF Creation
└─────────────┘
```

## 🛠️ Technology Stack

| Category | Technology |
|----------|------------|
| **Backend** | Java 11, Servlets, JSP |
| **Server** | Apache Tomcat 9.0.113 |
| **PDF Library** | Apache PDFBox 2.0.30 |
| **Image Processing** | Java ImageIO, WebP ImageIO |
| **Frontend** | Tailwind CSS, Vanilla JavaScript |
| **Build Tool** | Maven |

## 📦 Dependencies

- **Apache PDFBox** 2.0.30 - PDF generation
- **WebP ImageIO** 0.1.6 - WebP format support
- **Commons Logging** 1.2 - Logging framework
- **Servlet API** 4.0.1 - Web application framework

See [pom.xml](pom.xml) for complete dependency list.

## 🌐 Deploy to Railway

[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app)

### One-Click Deployment

1. Push this repository to GitHub
2. Sign up at [Railway](https://railway.app)
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Railway automatically detects Maven and deploys!

**See:** [Railway Deployment Guide](brain/railway-deployment.md) for detailed instructions.

## 📚 Documentation

Comprehensive documentation is available in the [`brain/`](brain/) folder:

- **[README.md](brain/README.md)** - Project overview and quick reference
- **[Architecture](brain/architecture.md)** - System design and data flows
- **[API Documentation](brain/api-documentation.md)** - Endpoint specifications
- **[Java Classes](brain/java-classes.md)** - Detailed class documentation
- **[Frontend](brain/frontend.md)** - JSP pages and UI components
- **[Dependencies](brain/dependencies.md)** - External libraries
- **[Deployment Guide](brain/deployment.md)** - Build and deployment instructions
- **[Railway Deployment](brain/railway-deployment.md)** - Cloud deployment guide
- **[Development Guide](brain/development-guide.md)** - Adding new features

## 🔧 Configuration

### Upload File Size Limits

Edit `src/main/java/controller/ImageServlet.java`:

```java
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 5,    // 5 MB
    maxFileSize = 1024 * 1024 * 50,         // 50 MB per file
    maxRequestSize = 1024 * 1024 * 300      // 300 MB total
)
```

### PDF Page Size

Edit `src/main/java/util/ImageUtils.java`:

```java
// A4 Portrait: 595 x 842 points
page = new PDPage(new PDRectangle(595, 842));

// A4 Landscape: 842 x 595 points
page = new PDPage(new PDRectangle(842, 595));
```

### Image Quality

Edit compression quality in `src/main/java/util/ImageUtils.java`:

```java
jpegParams.setCompressionQuality(0.85f); // 85% quality
```

## 🐛 Troubleshooting

### WebP Not Working

**Issue:** "No ImageIO readers for format webp"

**Solution:**
```bash
# Verify WebP library exists
ls src/main/webapp/WEB-INF/lib/webp-imageio-*.jar

# Restart Tomcat
```

See [WEBP_LIBRARY_SETUP.md](WEBP_LIBRARY_SETUP.md) for details.

### OutOfMemoryError

**Issue:** Large images cause memory errors

**Solution:**
```bash
# Increase Tomcat heap size
set JAVA_OPTS=-Xmx2048m
```

### Build Errors

**Issue:** Maven compilation fails

**Solution:**
```bash
# Clean and rebuild
mvn clean package

# Verify Java version
java -version  # Should be 11+
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 Development

### Project Structure

```
image-utility/
├── src/
│   └── main/
│       ├── java/
│       │   ├── controller/      # Servlets
│       │   └── util/           # Business logic
│       └── webapp/             # Web resources
│           ├── WEB-INF/
│           ├── index.jsp       # Upload page
│           └── result.jsp      # Download page
├── brain/                      # Documentation
├── pom.xml                     # Maven configuration
└── deploy_and_run.bat          # Local deployment script
```

### Adding New Features

See [Development Guide](brain/development-guide.md) for:
- Adding watermarks
- Image filters
- Page numbers
- Custom output filenames
- And more!

## 📊 Performance

- **Build Time:** ~30 seconds (Maven)
- **Startup Time:** ~2 seconds (Tomcat)
- **Conversion Speed:** ~1 second per image (depends on size)
- **Memory Usage:** ~200 MB baseline + ~50 MB per concurrent request

## 🔒 Security

- ✅ File type validation
- ✅ Size limit enforcement
- ✅ Path traversal prevention
- ✅ Unique filename generation
- ⚠️ No authentication (add if needed)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

**Project:** Image to PDF Converter  
**Version:** 1.0.0  
**Created:** 2025

## 🙏 Acknowledgments

- **Apache PDFBox** - PDF library
- **WebP ImageIO** - WebP support
- **Tailwind CSS** - UI framework
- **Railway** - Deployment platform

## 📞 Support

For issues and questions:
- Check [Documentation](brain/)
- Review [Troubleshooting](#-troubleshooting)
- See [Development Guide](brain/development-guide.md)

---

**⭐ If you find this project useful, please give it a star!**
