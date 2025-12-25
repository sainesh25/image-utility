# API Documentation

## Servlet Endpoints

This document describes all HTTP endpoints exposed by the application.

---

## Base URL

**Local Development:**
```
http://localhost:8080/project-image-utility-tool/
```

**Context Path:** `/project-image-utility-tool`

---

## Endpoints

### 1. Image Upload & Conversion

**Endpoint:** `/ImageServlet`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data`  
**Purpose:** Upload images and convert to PDF

#### Request

**Form Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| image | file | Yes | Image file(s) to convert |

**HTML Form Example:**

```html
<form action="ImageServlet" method="post" enctype="multipart/form-data">
    <input type="file" name="image" accept="image/*" multiple required>
    <button type="submit">Convert to PDF</button>
</form>
```

**JavaScript Example:**

```javascript
const formData = new FormData();
formData.append('image', file1);
formData.append('image', file2); // Multiple files allowed

fetch('/project-image-utility-tool/ImageServlet', {
    method: 'POST',
    body: formData
})
.then(response => {
    // Servlet performs redirect, follow it
})
.catch(error => console.error(error));
```

#### Request Constraints

| Constraint | Value | Description |
|------------|-------|-------------|
| Max file size | 50 MB | Per individual file |
| Max request size | 300 MB | Total for all files |
| In-memory threshold | 5 MB | Files larger written to disk |
| Supported formats | image/* | JPEG, PNG, WebP, etc. |

#### Response

**Success:**
- **Type:** Forward to `result.jsp`
- **Attribute:** `result` = PDF filename
- **User sees:** Success page with download link

**Error:**
- **Type:** Redirect to `index.jsp?error={message}`
- **Status:** 302 Found
- **Parameter:** URL-encoded error message

#### Error Responses

| Scenario | Error Message |
|----------|---------------|
| No files uploaded | "No valid image files were uploaded. Please select at least one image file." |
| File too large | "File size limit exceeded. Maximum file size is 50 MB. Please choose a smaller image." |
| Non-image file | Files silently skipped during processing |
| Conversion failed | "Error converting images to PDF: {details}" |
| Other errors | "An error occurred: {details}" |

#### Example Responses

**Success Flow:**
```
POST /ImageServlet
  ↓
Forward to result.jsp with attribute:
  result = "output_1703516789123.pdf"
  ↓
User sees download page
```

**Error Flow:**
```
POST /ImageServlet
  ↓
Redirect to:
  index.jsp?error=File+size+limit+exceeded.+Maximum+file+size+is+50+MB.
  ↓
User sees error banner on upload page
```

---

### 2. File Download

**Endpoint:** `/download`  
**Method:** `GET`  
**Purpose:** Download generated PDF file

#### Request

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | string | Yes | Filename to download |

**Example:**

```
GET /download?file=output_1703516789123.pdf
```

**HTML Link:**

```html
<a href="download?file=output_1703516789123.pdf" download>
    Download PDF
</a>
```

**JavaScript:**

```javascript
const filename = 'output_1703516789123.pdf';
window.location.href = `/project-image-utility-tool/download?file=${encodeURIComponent(filename)}`;
```

#### Security Validations

1. **Filename Required**
   - Returns 400 Bad Request if missing

2. **Path Traversal Prevention**
   - Blocks `..`, `/`, `\` characters
   - Returns 403 Forbidden if detected

3. **File Existence**
   - Checks file exists in uploads/
   - Returns 404 Not Found if missing

#### Response

**Success:**
- **Status:** 200 OK
- **Content-Type:** Based on file extension (see table below)
- **Content-Disposition:** `attachment; filename="{filename}"`
- **Content-Length:** File size in bytes
- **Body:** Binary file stream

**Content Types:**

| Extension | MIME Type |
|-----------|-----------|
| .pdf | application/pdf |
| .jpg, .jpeg | image/jpeg |
| .png | image/png |
| .webp | image/webp |
| others | application/octet-stream |

**Error Responses:**

| Status | Reason |
|--------|--------|
| 400 Bad Request | Missing file parameter |
| 403 Forbidden | Invalid file path (security) |
| 404 Not Found | File doesn't exist |

#### Example Response Headers

```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="output_1703516789123.pdf"
Content-Length: 524288
Transfer-Encoding: chunked
```

---

## Complete User Flow

### Conversion Flow

```
1. User opens: http://localhost:8080/project-image-utility-tool/
   ↓
2. Browser loads: index.jsp

3. User selects images and clicks "Convert"
   ↓
4. Browser POSTs to: /ImageServlet
   ↓
5. Server processes:
   - Validates files
   - Saves to uploads/
   - Converts to PDF
   - Cleans up temp images
   ↓
6. Server forwards to: result.jsp
   ↓
7. User sees success page

8. User clicks "Download"
   ↓
9. Browser GETs: /download?file=output_xxx.pdf
   ↓
10. Server streams file
    ↓
11. Browser saves PDF to Downloads
```

### Error Flow

```
1. User uploads oversized file
   ↓
2. POST /ImageServlet
   ↓
3. @MultipartConfig throws IllegalStateException
   ↓
4. Servlet catches exception
   ↓
5. Redirect to: index.jsp?error=File+size+limit+exceeded...
   ↓
6. index.jsp displays error banner
   ↓
7. User sees error message
```

---

## Internal API (Java)

### ImageUtils Public Methods

#### imageToPdf()

```java
public static String imageToPdf(String imgPath, String output) 
    throws Exception
```

**Purpose:** Convert single image to PDF

**Parameters:**
- `imgPath` - Path to image file
- `output` - Output PDF path

**Returns:** String - Path to created PDF

**Throws:** Exception on any error

**Example:**

```java
String pdf = ImageUtils.imageToPdf(
    "C:/uploads/image.jpg",
    "C:/uploads/output.pdf"
);
```

---

#### imagesToPdf()

```java
public static String imagesToPdf(String[] imgPaths, String output) 
    throws Exception
```

**Purpose:** Convert multiple images to single PDF

**Parameters:**
- `imgPaths` - Array of image file paths
- `output` - Output PDF path

**Returns:** String - Path to created PDF

**Throws:**
- IllegalArgumentException if imgPaths is null/empty
- Exception on processing errors

**Example:**

```java
String[] images = {
    "C:/uploads/img1.jpg",
    "C:/uploads/img2.png",
    "C:/uploads/img3.webp"
};

String pdf = ImageUtils.imagesToPdf(
    images,
    "C:/uploads/combined.pdf"
);
```

**Processing Details:**

1. Each image is processed independently
2. Failed images are skipped (logged to stderr)
3. Page orientation matches image orientation
4. Images are centered with 20px margins
5. EXIF orientation is automatically handled
6. Images are optimized and compressed

**Page Sizes:**

| Orientation | Dimensions (points) | Size |
|-------------|---------------------|------|
| Portrait | 595 x 842 | A4 |
| Landscape | 842 x 595 | A4 Rotated |

---

## Testing the API

### Using cURL

**Upload Image:**

```bash
curl -X POST \
  -F "image=@/path/to/image1.jpg" \
  -F "image=@/path/to/image2.png" \
  http://localhost:8080/project-image-utility-tool/ImageServlet
```

**Download File:**

```bash
curl -O \
  "http://localhost:8080/project-image-utility-tool/download?file=output_123.pdf"
```

### Using Postman

**Upload Endpoint:**

1. Method: POST
2. URL: `http://localhost:8080/project-image-utility-tool/ImageServlet`
3. Body → form-data
4. Add key: `image` (type: File)
5. Select file(s)
6. Send

**Download Endpoint:**

1. Method: GET
2. URL: `http://localhost:8080/project-image-utility-tool/download`
3. Params: `file` = `output_xxx.pdf`
4. Send
5. Save response as binary file

---

## Rate Limiting

**Current:** None

**Recommendation for Production:**

```java
// Add rate limiting per IP
// Example: Max 10 conversions per minute
```

---

## Session Management

**Current:** Stateless (no sessions)

**Characteristics:**
- Each request is independent
- No user tracking
- Files stored with unique timestamps
- Can scale horizontally

---

## File Cleanup

**Current:** Manual cleanup only

**Cleanup Points:**
1. Temporary images deleted after PDF creation
2. Temporary JPEG files deleted after embedding
3. Generated PDFs remain in uploads/ indefinitely

**Recommendation:**

```java
// Add scheduled cleanup task
// Delete files older than 1 hour
```

---

## API Security Considerations

### Current Security

✅ **Implemented:**
- File type validation (MIME type check)
- File size limits (50MB/300MB)
- Path traversal prevention in download
- Unique filename generation (prevents overwrites)

⚠️ **Missing:**
- Authentication/Authorization
- Rate limiting
- CSRF protection
- Virus scanning
- File cleanup scheduling
- Content Security Policy headers

### Recommendations

1. **Add Authentication:**
   ```java
   @WebServlet("/ImageServlet")
   // Add authentication filter
   ```

2. **Implement Rate Limiting:**
   ```java
   // Per-IP request throttling
   ```

3. **Add CSRF Tokens:**
   ```html
   <input type="hidden" name="csrf_token" value="${sessionScope.csrf}">
   ```

4. **Scheduled Cleanup:**
   ```java
   // Cron job to delete old files
   ```

---

## API Versioning

**Current:** No versioning

**For Future:**

```
/v1/ImageServlet
/v1/download
```

---

**Next:** [Frontend Documentation](./frontend.md)
