# Dependencies Documentation

## Overview

This document details all external libraries and dependencies used in the image-utility project.

---

## Dependency List

### 1. Apache PDFBox

**File:** `pdfbox-2.0.30.jar`  
**Location:** `src/main/webapp/WEB-INF/lib/`  
**Version:** 2.0.30  
**Size:** ~2.8 MB  

**Official Website:** https://pdfbox.apache.org/  
**Maven Central:** https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox/2.0.30

#### Purpose

PDF document creation, manipulation, and rendering library.

#### Usage in Project

```java
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
```

**Used For:**
- Creating PDF documents (`PDDocument`)
- Creating PDF pages (`PDPage`)
- Setting page sizes (`PDRectangle`)
- Embedding images (`PDImageXObject`, `LosslessFactory`)
- Drawing on pages (`PDPageContentStream`)

#### Key Classes Used

| Class | Purpose |
|-------|---------|
| `PDDocument` | Main PDF document container |
| `PDPage` | Individual PDF page |
| `PDRectangle` | Page dimensions (A4: 595x842 pts) |
| `PDPageContentStream` | Draw/write content on pages |
| `PDImageXObject` | Image object for embedding |
| `LosslessFactory` | Create images without quality loss |

#### Example Usage

```java
PDDocument doc = new PDDocument();
PDPage page = new PDPage(new PDRectangle(595, 842));
doc.addPage(page);

PDImageXObject img = PDImageXObject.createFromFile("image.jpg", doc);
PDPageContentStream cs = new PDPageContentStream(doc, page);
cs.drawImage(img, x, y, width, height);
cs.close();

doc.save("output.pdf");
doc.close();
```

#### License

**Apache License 2.0**
- ✅ Commercial use allowed
- ✅ Modification allowed
- ✅ Distribution allowed
- ✅ Patent use allowed
- ⚠️ Requires attribution
- ⚠️ Requires license inclusion

---

### 2. Commons Logging

**File:** `commons-logging-1.2.jar`  
**Location:** `src/main/webapp/WEB-INF/lib/`  
**Version:** 1.2  
**Size:** ~60 KB

**Official Website:** https://commons.apache.org/proper/commons-logging/  
**Maven Central:** https://mvnrepository.com/artifact/commons-logging/commons-logging/1.2

#### Purpose

Logging abstraction layer used by Apache PDFBox.

#### Usage in Project

**Indirect dependency** - Required by PDFBox but not directly used in application code.

PDFBox uses it internally for logging:
- Debug information
- Warning messages
- Error reporting

#### Configuration

No configuration needed for this project (uses defaults).

**Default Behavior:**
- Logs to System.err
- INFO level and above

#### License

**Apache License 2.0** (same as PDFBox)

---

### 3. WebP ImageIO

**File:** `webp-imageio-0.1.6.jar`  
**Location:** `src/main/webapp/WEB-INF/lib/`  
**Version:** 0.1.6  
**Size:** ~200 KB

**GitHub:** https://github.com/sejda-pdf/webp-imageio  
**Maven Central:** https://mvnrepository.com/artifact/com.github.gotson/webp-imageio-core/0.1.6

> **Note:** There are two WebP ImageIO libraries:
> - `com.github.gotson:webp-imageio-core` (recommended, version 0.4.4)
> - `org.sejda.imageio:webp-imageio` (older, version 0.1.6)
> 
> The project uses version 0.1.6. Consider upgrading to 0.4.4 for better performance.

#### Purpose

Adds WebP image format support to Java ImageIO.

#### Usage in Project

**Automatic integration** with Java ImageIO:

```java
// No explicit imports needed
// ImageIO automatically discovers and uses the plugin

BufferedImage image = ImageIO.read(new File("image.webp"));
// WebP format is now supported automatically
```

**How it Works:**

1. Library includes a Service Provider Interface (SPI) for ImageIO
2. Java's `ImageIO` class automatically discovers registered plugins
3. When reading a WebP file, ImageIO uses this plugin automatically

#### Supported Operations

| Operation | Supported |
|-----------|-----------|
| Read WebP | ✅ Yes |
| Write WebP | ✅ Yes |
| Lossy compression | ✅ Yes |
| Lossless compression | ✅ Yes |
| Transparency (alpha) | ✅ Yes |
| Animation | ❌ No |
| Metadata (EXIF) | ⚠️ Limited |

#### Setup

See `WEBP_LIBRARY_SETUP.md` for installation instructions.

#### License

**Apache License 2.0**

---

## Runtime Dependencies

### 4. Java Servlet API

**Provider:** Apache Tomcat  
**Version:** 4.0 (Servlet API), 2.3 (JSP API)  
**Location:** `TOMCAT_HOME/lib/servlet-api.jar`

**Not bundled** with application - provided by servlet container.

#### Usage

```java
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
```

**Used For:**
- `HttpServlet` - Base servlet class
- `HttpServletRequest` / `HttpServletResponse` - Request/response handling
- `@WebServlet` - Servlet URL mapping
- `@MultipartConfig` - File upload configuration
- `Part` - Multipart file part handling

---

### 5. Java ImageIO

**Provider:** Java Standard Library (JDK)  
**Version:** Bundled with Java 11+  
**Location:** `java.base` module

#### Usage

```java
import javax.imageio.*;
import javax.imageio.stream.*;
```

**Used For:**
- Reading images (`ImageIO.read()`)
- Image format detection
- Subsampling (memory optimization)
- JPEG compression
- Image reader/writer plugins

---

### 6. Java AWT

**Provider:** Java Standard Library (JDK)  
**Version:** Bundled with Java 11+  
**Location:** `java.desktop` module

#### Usage

```java
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
```

**Used For:**
- Image manipulation (`BufferedImage`)
- Rotation and scaling (`Graphics2D`, `AffineTransform`)
- Color space conversion (RGB)
- Rendering quality hints

---

## Build Dependencies

### Maven (Optional)

**Not required** for this project, but can be used to download libraries.

**Example:**

```bash
mvn dependency:copy -Dartifact=com.github.gotson:webp-imageio-core:0.4.4 \
    -DoutputDirectory=src/main/webapp/WEB-INF/lib
```

---

## Frontend Dependencies (CDN)

### 7. Tailwind CSS

**Source:** CDN (https://cdn.tailwindcss.com)  
**Version:** Latest (auto-updated)  
**Size:** ~50 KB (gzipped)

**Usage:**
```html
<script src="https://cdn.tailwindcss.com"></script>
```

**Purpose:** CSS utility framework for styling

**Considerations:**
- ⚠️ CDN version is for development only
- ⚠️ Production should use build process
- ⚠️ No version pinning (always latest)

---

### 8. Google Fonts (Inter)

**Source:** Google Fonts CDN  
**Version:** Variable font (weights 400-700)  
**Size:** ~10 KB

**Usage:**
```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
```

**Purpose:** Typography (sans-serif font family)

---

## Dependency Tree

```
image-utility
├── Apache PDFBox 2.0.30 (compile)
│   └── Commons Logging 1.2 (runtime)
├── WebP ImageIO 0.1.6 (compile)
├── Java Servlet API 4.0 (provided by Tomcat)
├── Java ImageIO (JDK)
├── Java AWT (JDK)
└── Frontend (CDN)
    ├── Tailwind CSS (runtime)
    └── Google Fonts Inter (runtime)
```

---

## Dependency Management

### Current Approach

**Manual management** - JAR files stored in `WEB-INF/lib/`

**Pros:**
- ✅ No build tool required
- ✅ Simple project structure
- ✅ Explicit dependency control

**Cons:**
- ❌ Manual updates required
- ❌ No automatic transitive dependencies
- ❌ No version conflict resolution

### Alternative: Maven

**pom.xml example:**

```xml
<dependencies>
    <!-- PDF Generation -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.30</version>
    </dependency>
    
    <!-- WebP Support -->
    <dependency>
        <groupId>com.github.gotson</groupId>
        <artifactId>webp-imageio-core</artifactId>
        <version>0.4.4</version>
    </dependency>
    
    <!-- Servlet API (provided) -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## Version Compatibility

### Java Version

**Required:** Java 11+  
**Tested:** Java 11  
**Recommended:** Java 11 or Java 17 LTS

**Compilation:**
```bash
javac -source 11 -target 11 ...
```

### Tomcat Version

**Required:** Tomcat 9.0+  
**Tested:** Tomcat 9.0.113  
**Compatible:** Tomcat 9.0.x, 10.0.x (with Jakarta namespace migration)

---

## Security Considerations

### Dependency Vulnerabilities

**Current Status:** Unknown (no automated scanning)

**Recommendations:**

1. **Check for CVEs:**
   - PDFBox 2.0.30: Check https://nvd.nist.gov/
   - Commons Logging 1.2: Generally safe
   - WebP ImageIO 0.1.6: Check GitHub issues

2. **Update Strategy:**
   ```bash
   # Check latest versions
   PDFBox: Currently 2.0.30 (check for 2.0.31+)
   WebP ImageIO: Upgrade to 0.4.4
   ```

3. **Automated Scanning:**
   - Use OWASP Dependency-Check
   - Or GitHub Dependabot (if using git repo)

---

## Updating Dependencies

### Update Process

**1. Check Latest Versions:**

| Library | Current | Latest | Status |
|---------|---------|--------|--------|
| PDFBox | 2.0.30 | Check Maven | ? |
| WebP ImageIO | 0.1.6 | 0.4.4+ | ⚠️ Outdated |
| Commons Logging | 1.2 | 1.2 | ✅ Current |

**2. Download New JAR:**

```bash
# Option 1: Maven
mvn dependency:copy -Dartifact=GROUP:ARTIFACT:VERSION \
    -DoutputDirectory=src/main/webapp/WEB-INF/lib

# Option 2: Manual
# Download from Maven Central
```

**3. Replace Old JAR:**

```bash
# Backup old version
mv webp-imageio-0.1.6.jar webp-imageio-0.1.6.jar.bak

# Add new version
cp /path/to/webp-imageio-0.4.4.jar src/main/webapp/WEB-INF/lib/
```

**4. Test:**

```bash
# Rebuild and test
.\deploy_and_run.bat

# Test WebP conversion
# Test PDF generation
```

---

## License Compliance

### Summary

| Library | License | Commercial Use | Attribution Required |
|---------|---------|----------------|---------------------|
| PDFBox | Apache 2.0 | ✅ Yes | Yes |
| Commons Logging | Apache 2.0 | ✅ Yes | Yes |
| WebP ImageIO | Apache 2.0 | ✅ Yes | Yes |

### Required Attribution

**Include in NOTICE file or documentation:**

```
This product includes software developed by:
- The Apache Software Foundation (http://www.apache.org/)
  - Apache PDFBox 2.0.30
  - Apache Commons Logging 1.2
- WebP ImageIO contributors
  - WebP ImageIO 0.1.6
```

---

## Troubleshooting

### Common Issues

**1. WebP Not Working**

**Error:** "No ImageIO readers for format webp"

**Solution:**
```bash
# Verify JAR is present
ls src/main/webapp/WEB-INF/lib/webp-imageio-*.jar

# Restart Tomcat
# Clear Tomcat work directory
```

**2. PDF Generation Fails**

**Error:** "ClassNotFoundException: org.apache.pdfbox..."

**Solution:**
```bash
# Verify all PDFBox JARs are present
ls src/main/webapp/WEB-INF/lib/ | grep -E "(pdfbox|commons-logging)"

# Check classpath in deployment
```

**3. OutOfMemoryError**

**Error:** "java.lang.OutOfMemoryError: Java heap space"

**Solution:**
```bash
# Increase Tomcat heap size
# Edit catalina.bat:
set JAVA_OPTS=-Xms512m -Xmx2048m
```

---

## Dependency Alternatives

### Alternative PDF Libraries

| Library | Pros | Cons |
|---------|------|------|
| **iText** | Feature-rich, mature | AGPL or commercial license |
| **OpenPDF** | iText fork, MIT | Less actively maintained |
| **Apache FOP** | XSL-FO based | Complex, XML-focused |

**Why PDFBox:** Apache 2.0 license, good Java support, active development

---

### Alternative WebP Libraries

| Library | Version | Recommendation |
|---------|---------|----------------|
| webp-imageio-core | 0.4.4 | ✅ **Upgrade to this** |
| webp-imageio | 0.1.6 | 🟡 Current (legacy) |

---

**Next:** [Deployment Guide](./deployment.md)
