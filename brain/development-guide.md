# Development Guide

## Overview

This guide explains how to add new features and extend the image-utility application.

---

## Development Environment Setup

### IDE Setup

**Recommended IDEs:**
- IntelliJ IDEA
- Eclipse
- VS Code (with Java extensions)

**VS Code Extensions:**
```json
{
    "recommendations": [
        "vscjava.vscode-java-pack",
        "redhat.java",
        "vscjava.vscode-java-debug",
        "vscjava.vscode-maven"
    ]
}
```

### Project Import

**IntelliJ IDEA:**
1. File → Open
2. Select project directory
3. Configure JDK 11
4. Add Tomcat configuration

**Eclipse:**
1. File → Import → Existing Projects
2. Select root directory
3. Configure build path
4. Add libraries from WEB-INF/lib/

---

## Code Structure

### Package Organization

```
src/main/java/
├── controller/          # HTTP request handlers
│   ├── ImageServlet     # Upload & conversion
│   └── DownloadServlet  # File downloads
└── util/                # Business logic
    └── ImageUtils       # Image processing
```

### Adding a New Servlet

**1. Create servlet class:**

```java
package controller;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@SuppressWarnings("serial")
@WebServlet("/YourServlet")
public class YourServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // Your logic here
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // Your logic here
    }
}
```

**2. Compile:**

```bash
javac -d build/classes -cp "..." src/main/java/controller/YourServlet.java
```

**3. Deploy:**

```bash
.\deploy_and_run.bat
```

**4. Access:**

```
http://localhost:8080/project-image-utility-tool/YourServlet
```

---

## Common Modifications

### 1. Change Upload File Size Limits

**File:** `src/main/java/controller/ImageServlet.java` (lines 13-17)

```java
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 5,    // Change: In-memory threshold
    maxFileSize = 1024 * 1024 * 100,        // Change: 100 MB per file
    maxRequestSize = 1024 * 1024 * 500      // Change: 500 MB total
)
```

**Also update frontend:**

`src/main/webapp/index.jsp` (line 72, 100):

```javascript
const maxSize = 100 * 1024 * 1024; // 100 MB
```

---

### 2. Change PDF Page Size

**File:** `src/main/java/util/ImageUtils.java` (lines 69-75)

```java
// Current: A4 (595 x 842 points)
// Letter: (612 x 792 points)
// Legal: (612 x 1008 points)

if (isPortrait) {
    page = new PDPage(new PDRectangle(612, 792)); // Letter Portrait
} else {
    page = new PDPage(new PDRectangle(792, 612)); // Letter Landscape
}
```

**Common Page Sizes:**

| Size | Portrait (pts) | Landscape (pts) |
|------|---------------|----------------|
| A4 | 595 x 842 | 842 x 595 |
| Letter | 612 x 792 | 792 x 612 |
| Legal | 612 x 1008 | 1008 x 612 |
| A3 | 842 x 1191 | 1191 x 842 |

---

### 3. Adjust Image Quality

**File:** `src/main/java/util/ImageUtils.java` (line 125)

```java
// Current: 85% quality
jpegParams.setCompressionQuality(0.85f);

// Higher quality (larger file):
jpegParams.setCompressionQuality(0.95f);

// Lower quality (smaller file):
jpegParams.setCompressionQuality(0.70f);
```

---

### 4. Change Maximum Image Dimension

**File:** `src/main/java/util/ImageUtils.java` (line 39, 215)

```java
// Current: 2048 pixels
BufferedImage bufferedImage = readSubsampledImage(imageFile, 2048);

// Higher resolution:
BufferedImage bufferedImage = readSubsampledImage(imageFile, 4096);

// Lower resolution (less memory):
BufferedImage bufferedImage = readSubsampledImage(imageFile, 1024);
```

---

### 5. Modify Page Margins

**File:** `src/main/java/util/ImageUtils.java` (lines 155-156, 170-171)

```java
// Current: 20px margins
float pageWidth = page.getMediaBox().getWidth() - 40;  // 20px each side
float pageHeight = page.getMediaBox().getHeight() - 40;

// Change to 50px margins:
float pageWidth = page.getMediaBox().getWidth() - 100;  // 50px each side
float pageHeight = page.getMediaBox().getHeight() - 100;

// Center calculation:
float x = (pageWidth - scaledWidth) / 2 + 50;  // Half margin
float y = (pageHeight - scaledHeight) / 2 + 50;
```

---

## Adding New Features

### Feature 1: Add Watermark to PDFs

**1. Add method to ImageUtils:**

```java
private static void addWatermark(PDDocument doc, String text) throws IOException {
    for (PDPage page : doc.getPages()) {
        PDPageContentStream cs = new PDPageContentStream(
            doc, page, PDPageContentStream.AppendMode.APPEND, true, true
        );
        
        cs.setNonStrokingColor(200, 200, 200); // Light gray
        cs.setFont(PDType1Font.HELVETICA, 50);
        cs.beginText();
        cs.setTextMatrix(Matrix.getRotateInstance(
            Math.toRadians(45), 200, 300
        ));
        cs.showText(text);
        cs.endText();
        cs.close();
    }
}
```

**2. Call from imagesToPdf:**

```java
// After all pages added (before doc.save):
addWatermark(doc, "DRAFT");
doc.save(output);
```

---

### Feature 2: Add Image Filters

**1. Create filter method:**

```java
private static BufferedImage applyGrayscale(BufferedImage original) {
    BufferedImage grayscale = new BufferedImage(
        original.getWidth(), original.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY
    );
    Graphics2D g = grayscale.createGraphics();
    g.drawImage(original, 0, 0, null);
    g.dispose();
    return grayscale;
}
```

**2. Add filter option to form:**

```html
<select name="filter">
    <option value="none">No Filter</option>
    <option value="grayscale">Grayscale</option>
    <option value="sepia">Sepia</option>
</select>
```

**3. Apply in ImageServlet:**

```java
String filter = req.getParameter("filter");
if ("grayscale".equals(filter)) {
    // Apply filter before conversion
}
```

---

### Feature 3: Add Page Numbers

**1. Add method:**

```java
private static void addPageNumbers(PDDocument doc) throws IOException {
    int pageNum = 1;
    for (PDPage page : doc.getPages()) {
        PDPageContentStream cs = new PDPageContentStream(
            doc, page, PDPageContentStream.AppendMode.APPEND, true, true
        );
        
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.newLineAtOffset(
            page.getMediaBox().getWidth() - 50, 20
        );
        cs.showText("Page " + pageNum);
        cs.endText();
        cs.close();
        
        pageNum++;
    }
}
```

**2. Call before saving:**

```java
addPageNumbers(doc);
doc.save(output);
```

---

### Feature 4: Support Custom Output Filename

**1. Add field to form:**

```html
<input type="text" name="outputName" 
       placeholder="output.pdf" 
       pattern="[a-zA-Z0-9_-]+\.pdf">
```

**2. Use in ImageServlet:**

```java
String outputName = req.getParameter("outputName");
if (outputName == null || outputName.isEmpty()) {
    outputName = "output_" + System.currentTimeMillis() + ".pdf";
}

String resultFile = ImageUtils.imagesToPdf(
    imgPathsArray,
    uploadPath + outputName
);
```

---

### Feature 5: Add Image Reordering

**1. Add jQuery UI sortable:**

```html
<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>

<div id="imageList" class="sortable">
    <!-- Preview thumbnails -->
</div>

<script>
$("#imageList").sortable({
    update: function(event, ui) {
        updateOrder();
    }
});
</script>
```

**2. Send order to servlet:**

```javascript
const order = $("#imageList").sortable("toArray");
// Send as hidden form field
```

---

## Testing

### Manual Testing

**Test Checklist:**

- [ ] Upload single image
- [ ] Upload multiple images
- [ ] Upload large file (near 50MB limit)
- [ ] Upload oversized file (should see error)
- [ ] Upload non-image file (should see error)
- [ ] Test WebP format
- [ ] Test JPEG with EXIF orientation
- [ ] Test PNG without EXIF
- [ ] Download generated PDF
- [ ] Verify PDF opens correctly
- [ ] Check image orientation in PDF
- [ ] Test error handling

### Automated Testing

**JUnit test example:**

```java
import org.junit.Test;
import static org.junit.Assert.*;

public class ImageUtilsTest {
    
    @Test
    public void testImageToPdf() throws Exception {
        String result = ImageUtils.imageToPdf(
            "test-images/sample.jpg",
            "test-output/result.pdf"
        );
        
        File pdfFile = new File(result);
        assertTrue("PDF should be created", pdfFile.exists());
        assertTrue("PDF should not be empty", pdfFile.length() > 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testImagesToPdfNull() throws Exception {
        ImageUtils.imagesToPdf(null, "output.pdf");
    }
}
```

---

## Debugging

### Enable Verbose Logging

**Add to ImageUtils:**

```java
private static final boolean DEBUG = true;

private static void log(String message) {
    if (DEBUG) {
        System.out.println("[ImageUtils] " + message);
    }
}

// Usage:
log("Processing image: " + imgPath);
log("EXIF orientation: " + orientation);
```

### Debug EXIF Issues

**Use TestExif utility:**

```bash
cd test
javac TestExif.java
java TestExif path/to/problem-image.jpg
```

**Output shows:**
- JPEG signature
- EXIF markers
- Orientation value
- Parsing steps

---

## Performance Optimization

### 1. Reduce Memory Usage

**Current:** Images subsampled to 2048px

**For lower memory:**

```java
BufferedImage bufferedImage = readSubsampledImage(imageFile, 1024);
```

### 2. Faster Compression

**Use lower quality:**

```java
jpegParams.setCompressionQuality(0.70f); // Faster, smaller
```

### 3. Parallel Processing

**Process multiple images in parallel:**

```java
import java.util.concurrent.*;

ExecutorService executor = Executors.newFixedThreadPool(4);
List<Future<ProcessedImage>> futures = new ArrayList<>();

for (String imgPath : imgPaths) {
    futures.add(executor.submit(() -> processImage(imgPath)));
}

for (Future<ProcessedImage> future : futures) {
    ProcessedImage img = future.get();
    addToPdf(doc, img);
}

executor.shutdown();
```

---

## Code Style

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `ImageServlet` |
| Methods | camelCase | `imagesToPdf()` |
| Variables | camelCase | `imagePaths` |
| Constants | UPPER_SNAKE_CASE | `MAX_FILE_SIZE` |
| Packages | lowercase | `controller`, `util` |

### Comments

**Method documentation:**

```java
/**
 * Converts multiple images to a single PDF file.
 * 
 * @param imgPaths Array of image file paths
 * @param output Output PDF file path
 * @return Path to the generated PDF
 * @throws IllegalArgumentException if imgPaths is null or empty
 * @throws Exception if conversion fails
 */
public static String imagesToPdf(String[] imgPaths, String output) 
        throws Exception {
    // Implementation
}
```

---

## Git Workflow

### Branch Strategy

```bash
# Main branch
main

# Feature branches
feature/watermark-support
feature/image-filters
feature/custom-filename

# Bugfix branches
bugfix/exif-rotation
bugfix/memory-leak
```

### Commit Messages

**Format:**

```
[type]: Brief description

Detailed explanation if needed.

Relates to #issue-number
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Code restructure
- `test`: Add tests
- `chore`: Maintenance

**Examples:**

```bash
git commit -m "feat: Add watermark support to PDF generation"
git commit -m "fix: Correct EXIF orientation for landscape images"
git commit -m "docs: Update brain documentation with new features"
```

---

## Release Process

### Version Numbering

**Semantic Versioning:** `MAJOR.MINOR.PATCH`

- **MAJOR:** Breaking changes
- **MINOR:** New features (backward compatible)
- **PATCH:** Bug fixes

### Creating a Release

**1. Update version in code:**

```java
// Add to ImageUtils
public static final String VERSION = "1.1.0";
```

**2. Tag release:**

```bash
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0
```

**3. Create release notes:**

```markdown
# Release v1.1.0

## Features
- Added watermark support
- Implemented image filters

## Bug Fixes
- Fixed EXIF rotation for landscape images
- Corrected memory leak in image processing

## Changes
- Upgraded WebP library to 0.4.4
```

---

## Extending the Brain Documentation

**When adding new features, update:**

1. **README.md** - Add to feature list
2. **architecture.md** - Update diagrams if needed
3. **java-classes.md** - Document new classes/methods
4. **api-documentation.md** - Add new endpoints
5. **development-guide.md** - Add implementation guide

**Example:**

```markdown
### Feature: Watermark Support

**Added in:** v1.1.0  
**Location:** `ImageUtils.addWatermark()`

Usage:
\`\`\`java
// Add watermark before saving
addWatermark(doc, "CONFIDENTIAL");
\`\`\`
```

---

## Resources

### Documentation

- [Apache PDFBox](https://pdfbox.apache.org/docs/2.0.30/index.html)
- [Java Servlets](https://docs.oracle.com/javaee/7/tutorial/servlets.htm)
- [Tomcat Documentation](https://tomcat.apache.org/tomcat-9.0-doc/index.html)

### Tutorials

- [PDFBox Examples](https://pdfbox.apache.org/2.0/examples.html)
- [Servlet Tutorial](https://www.baeldung.com/intro-to-servlets)
- [ImageIO Guide](https://docs.oracle.com/javase/tutorial/2d/images/)

---

**End of Development Guide**
