# Architecture Documentation

## System Architecture

### Overview

The Image to PDF Converter follows a classic **Model-View-Controller (MVC)** pattern adapted for Java Servlets:

- **Model**: `ImageUtils.java` (business logic)
- **View**: JSP files (`index.jsp`, `result.jsp`)
- **Controller**: Servlets (`ImageServlet`, `DownloadServlet`)

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENT (Browser)                     │
│  - Modern UI with Tailwind CSS                          │
│  - File validation & upload                             │
└────────────────┬────────────────────────────────────────┘
                 │ HTTP Multipart POST
                 ▼
┌─────────────────────────────────────────────────────────┐
│              APACHE TOMCAT SERVER                        │
│                                                          │
│  ┌────────────────────────────────────────────────┐     │
│  │         CONTROLLER LAYER                       │     │
│  │                                                │     │
│  │  ┌────────────────┐    ┌──────────────────┐   │     │
│  │  │ ImageServlet   │    │ DownloadServlet  │   │     │
│  │  │                │    │                  │   │     │
│  │  │ - Upload       │    │ - File serving   │   │     │
│  │  │ - Validation   │    │ - Security       │   │     │
│  │  │ - Orchestration│    │ - Streaming      │   │     │
│  │  └────────┬───────┘    └────────▲─────────┘   │     │
│  │           │                     │             │     │
│  └───────────┼─────────────────────┼─────────────┘     │
│              │                     │                   │
│  ┌───────────▼─────────────────────┴─────────────┐     │
│  │         BUSINESS LOGIC LAYER                  │     │
│  │                                                │     │
│  │  ┌──────────────────────────────────────┐     │     │
│  │  │        ImageUtils.java               │     │     │
│  │  │                                      │     │     │
│  │  │  - imagesToPdf()                    │     │     │
│  │  │  - readSubsampledImage()            │     │     │
│  │  │  - getExifOrientation()             │     │     │
│  │  │  - compressAndOrientImage()         │     │     │
│  │  │  - parseExifOrientation()           │     │     │
│  │  └──────────────────────────────────────┘     │     │
│  │                                                │     │
│  └────────────────────────────────────────────────┘     │
│                                                          │
│  ┌────────────────────────────────────────────────┐     │
│  │           VIEW LAYER (JSP)                     │     │
│  │                                                │     │
│  │  ┌────────────┐    ┌──────────────┐           │     │
│  │  │ index.jsp  │    │ result.jsp   │           │     │
│  │  │            │    │              │           │     │
│  │  │ - Upload UI│    │ - Success UI │           │     │
│  │  │ - Validator│    │ - Download   │           │     │
│  │  └────────────┘    └──────────────┘           │     │
│  │                                                │     │
│  └────────────────────────────────────────────────┘     │
│                                                          │
│  ┌────────────────────────────────────────────────┐     │
│  │        EXTERNAL LIBRARIES                      │     │
│  │                                                │     │
│  │  - Apache PDFBox 2.0.30 (PDF generation)      │     │
│  │  - WebP ImageIO 0.1.6 (WebP support)          │     │
│  │  - Commons Logging 1.2 (Logging)              │     │
│  │                                                │     │
│  └────────────────────────────────────────────────┘     │
│                                                          │
└─────────────────┬────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│               FILE SYSTEM                                │
│  - uploads/ (temporary storage)                         │
│  - Uploaded images (deleted after conversion)           │
│  - Generated PDFs (deleted after download timeout)      │
└─────────────────────────────────────────────────────────┘
```

## Data Flow

### Complete Request-Response Cycle

#### 1. Image Upload Flow

```
┌─────────┐
│ User    │ 1. Select images in browser
└────┬────┘
     │ 2. Submit form with files
     ▼
┌─────────────────────┐
│ ImageServlet        │
│ @MultipartConfig    │
└─────────┬───────────┘
          │ 3. Receive multipart request
          │
          ├─ 4. Validate file types
          ├─ 5. Check file sizes
          ├─ 6. Create uploads directory
          │
          ▼
     For each file:
          ├─ 7. Generate unique filename (timestamp_original)
          ├─ 8. Save to uploads/
          └─ 9. Add path to list
          │
          ▼
┌──────────────────────────────┐
│ ImageUtils.imagesToPdf()     │
└──────────┬───────────────────┘
           │
           ├─ For each image:
           │
           ▼
    ┌─────────────────────────────────┐
    │ 10. readSubsampledImage()       │
    │     - Load with subsampling     │
    │     - Memory optimization       │
    └───────────┬─────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │ 11. getExifOrientation()        │
    │     - Parse JPEG markers        │
    │     - Read APP1 EXIF data       │
    │     - Extract orientation tag   │
    └───────────┬─────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │ 12. Determine page size         │
    │     - Portrait: 595 x 842       │
    │     - Landscape: 842 x 595      │
    └───────────┬─────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │ 13. compressAndOrientImage()    │
    │     - Scale to max 2048px       │
    │     - Apply rotation            │
    │     - Convert to RGB            │
    └───────────┬─────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │ 14. Create temp JPEG            │
    │     - 85% compression quality   │
    │     - Save to temp file         │
    └───────────┬─────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │ 15. Create PDImageXObject       │
    │     - From compressed JPEG      │
    └───────────┬─────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │ 16. Add page to PDF             │
    │     - Scale to fit page         │
    │     - Center on page            │
    │     - 20px margins              │
    └───────────┬─────────────────────┘
                │
           └────┘ (Repeat for all images)
                │
                ▼
    ┌─────────────────────────────────┐
    │ 17. Save PDF document           │
    │     - output_[timestamp].pdf    │
    └───────────┬─────────────────────┘
                │
                ▼
┌───────────────────────────────────┐
│ 18. ImageServlet cleanup          │
│     - Delete temporary images     │
│     - Set result attribute        │
│     - Forward to result.jsp       │
└───────────┬───────────────────────┘
            │
            ▼
┌───────────────────────────┐
│ 19. result.jsp            │
│     - Display success     │
│     - Show download link  │
└───────────┬───────────────┘
            │
            ▼
     ┌─────────────┐
     │ User clicks │
     │  Download   │
     └──────┬──────┘
            │
            ▼
┌─────────────────────────────┐
│ 20. DownloadServlet.doGet() │
│     - Validate filename     │
│     - Security check        │
│     - Stream file           │
└─────────────────────────────┘
```

#### 2. Error Handling Flow

```
Any error occurs
     │
     ▼
┌─────────────────────────┐
│ Catch exception         │
│ - Generate error msg    │
│ - URL encode message    │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Redirect to index.jsp   │
│ ?error={message}        │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Display error banner    │
│ - Red alert box         │
│ - User-friendly message │
└─────────────────────────┘
```

## Component Architecture

### ImageServlet (Controller)

**Responsibilities:**
- HTTP request handling
- Multipart file parsing
- File validation (type, size)
- Temporary storage management
- Error handling and user feedback
- Resource cleanup

**Key Methods:**
- `doPost()`: Main request handler

**Configuration:**
```java
@MultipartConfig(
    fileSizeThreshold = 5MB,   // In-memory threshold
    maxFileSize = 50MB,        // Per file limit
    maxRequestSize = 300MB     // Total request limit
)
```

### DownloadServlet (Controller)

**Responsibilities:**
- File download handling
- Security validation (path traversal prevention)
- Content-type determination
- File streaming

**Security Features:**
- Prevents directory traversal (`..`, `/`, `\`)
- Validates file existence
- Sets proper content headers

### ImageUtils (Business Logic)

**Responsibilities:**
- Image processing and conversion
- EXIF metadata parsing
- Image optimization and compression
- PDF document generation
- Memory management

**Key Methods:**

| Method | Purpose | Input | Output |
|--------|---------|-------|--------|
| `imagesToPdf()` | Main conversion | String[] paths, output path | PDF path |
| `readSubsampledImage()` | Memory-safe image loading | File, max size | BufferedImage |
| `getExifOrientation()` | Read EXIF data | Image file | Orientation (1-8) |
| `compressAndOrientImage()` | Optimize & rotate | Image, orientation | Optimized image |
| `parseExifOrientation()` | Parse EXIF bytes | EXIF byte array | Orientation value |

### JSP Views

#### index.jsp (Upload Page)

**Features:**
- Modern gradient UI
- Multi-file selection
- Client-side validation
- File size display
- Error message display
- Tailwind CSS styling

**Client-side Validation:**
- File type checking
- Size limit enforcement (50MB per file)
- Total size calculation
- Real-time file info display

#### result.jsp (Success Page)

**Features:**
- Success confirmation
- Download button
- Return to upload link
- Animated success icon

## Memory Management Strategy

### Problem: Large Image Files

High-resolution images (e.g., 4080x3060) can consume massive memory if fully loaded.

### Solution: Multi-tier Optimization

```
1. SUBSAMPLING (readSubsampledImage)
   ├─ Read metadata first
   ├─ Calculate subsampling factor
   ├─ Read every Nth pixel
   └─ Target: 2048px max dimension

2. SCALING (compressAndOrientImage)
   ├─ Further reduce if needed
   ├─ Apply rotation
   └─ Convert to RGB (remove alpha)

3. JPEG COMPRESSION
   ├─ Create temp JPEG file
   ├─ 85% quality
   └─ Load into PDF from compressed file

4. CLEANUP
   ├─ Delete temp images
   ├─ Delete temp JPEG
   └─ Rely on garbage collection
```

### Memory Flow

```
Original Image (12MB, 4080x3060)
    ↓ Subsampling (factor 2)
Subsampled (3MB, 2040x1530)
    ↓ Rotation & scaling
Optimized (2MB, 1530x2040)
    ↓ JPEG compression (85%)
Compressed JPEG (500KB)
    ↓ PDF embedding
PDF page (~500KB per page)
```

## EXIF Orientation Handling

### EXIF Orientation Values

| Value | Description | Rotation |
|-------|-------------|----------|
| 1 | Normal | None |
| 2 | Flip horizontal | None |
| 3 | Rotate 180° | 180° |
| 4 | Flip vertical | None |
| 5 | Rotate 90° CW + flip | Complex |
| 6 | Rotate 90° CW | 90° CW |
| 7 | Rotate 270° CW + flip | Complex |
| 8 | Rotate 270° CW | 270° CW |

### Application Logic

```java
// ImageUtils.java lines 245-266
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
    case 8:  // 270° CW (90° CCW)
        t.translate(0, outputHeight);
        t.rotate(-Math.PI / 2);
        break;
}
```

### Fallback Strategy

For images without EXIF orientation (value = 1) but in landscape with phone camera aspect ratio (4:3):
```java
// Auto-rotate to portrait (orientation 6)
if (orientation == 1 && imgWidth > imgHeight) {
    float aspectRatio = (float) imgWidth / imgHeight;
    if (aspectRatio >= 1.3f && aspectRatio <= 1.4f) {
        orientation = 6; // Rotate 90° CW
    }
}
```

## Security Considerations

### 1. Path Traversal Prevention

**DownloadServlet** (lines 22-25):
```java
if (fileName.contains("..") || 
    fileName.contains("/") || 
    fileName.contains("\\")) {
    res.sendError(SC_FORBIDDEN);
}
```

### 2. File Type Validation

**ImageServlet** (lines 40-43):
```java
String contentType = part.getContentType();
if (contentType == null || !contentType.startsWith("image/")) {
    continue; // Skip non-image files
}
```

### 3. File Size Limits

- **Per file**: 50 MB
- **Total request**: 300 MB
- **In-memory threshold**: 5 MB

### 4. Unique Filenames

**ImageServlet** (line 36):
```java
String fileName = System.currentTimeMillis() + "_" + originalName;
```
Prevents file collisions and overwrites.

## Scalability Considerations

### Current Limitations

| Aspect | Current State | Limitation |
|--------|---------------|------------|
| Storage | File system (uploads/) | Not cloud-ready |
| Concurrency | Limited by file I/O | No async processing |
| Cleanup | Manual deletion | No scheduled cleanup |
| Session | None | Can't track user uploads |

### Future Improvements

1. **Cloud Storage Integration**
   - Upload to S3/Azure Blob
   - Generate pre-signed URLs

2. **Async Processing**
   - Queue-based conversion
   - Background workers
   - Progress tracking

3. **Scheduled Cleanup**
   - Cron job or scheduled task
   - Delete files older than N hours

4. **Caching**
   - Cache converted PDFs
   - Deduplicate identical images

5. **Load Balancing**
   - Stateless design (already done)
   - Can run multiple instances
   - Shared storage needed

---

**Next:** [API Documentation](./api-documentation.md)
