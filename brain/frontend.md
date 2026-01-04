# Frontend Documentation

## Overview

The application's frontend consists of two JSP pages styled with Tailwind CSS. The UI is modern, responsive, and user-friendly.

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| JSP | 2.3 | Server-side rendering |
| Tailwind CSS | Latest (CDN) | Styling framework |
| Google Fonts | Latest | Inter font family |
| Vanilla JavaScript | ES6+ | Client-side validation |

---

## Pages

### 1. index.jsp - Upload Page

**Path:** `src/main/webapp/index.jsp`  
**Lines:** 134  
**Purpose:** Main upload interface

#### Visual Design

```
┌─────────────────────────────────────────────┐
│    [Gradient Background]                    │
│                                             │
│   ┌──────────────────────────────────┐     │
│   │  🎨                              │     │
│   │                                  │     │
│   │  Image to PDF Converter          │     │
│   │  Convert single or multiple      │     │
│   │  images to PDF format            │     │
│   │                                  │     │
│   │  [ Error Banner (if present) ]   │     │
│   │                                  │     │
│   │  📁 Select Images                │     │
│   │  [Choose Files] No file chosen   │     │
│   │  Max: 50MB per file. Multiple OK │     │
│   │  2 images selected (5.23 MB)     │     │
│   │                                  │     │
│   │  [⚡ Convert to PDF (button)]    │     │
│   │                                  │     │
│   └──────────────────────────────────┘     │
│                                             │
└─────────────────────────────────────────────┘
```

#### HTML Structure

```html
<!DOCTYPE html>
<html>
<head>
    <title>Image Utility</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4">
    <!-- Content -->
</body>
</html>
```

#### Key Components

**1. Header Section (lines 15-25)**

```html
<div class="text-center mb-8">
    <div class="inline-flex items-center justify-center w-16 h-16 
                bg-gradient-to-br from-blue-500 to-purple-600 
                rounded-2xl mb-4 shadow-lg">
        <!-- Icon SVG -->
    </div>
    <h1 class="text-3xl font-bold bg-gradient-to-r from-blue-600 
               to-purple-600 bg-clip-text text-transparent mb-2">
        Image to PDF Converter
    </h1>
    <p class="text-gray-500 text-sm">
        Convert single or multiple images to PDF format
    </p>
</div>
```

**Features:**
- Gradient icon background
- Gradient text effect
- Descriptive subtitle

**2. Error Banner (lines 27-37)**

```html
<% String error = request.getParameter("error"); %>
<% if (error != null && !error.isEmpty()) { %>
<div class="mb-6 p-4 bg-red-50 border-2 border-red-200 rounded-xl">
    <div class="flex items-start gap-3">
        <svg class="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5">
            <!-- Warning icon -->
        </svg>
        <p class="text-sm text-red-700 font-medium"><%= error %></p>
    </div>
</div>
<% } %>
```

**Features:**
- Only shown when error parameter exists
- Red color scheme for errors
- Warning icon
- URL-decoded error message

**3. Upload Form (lines 39-64)**

```html
<form action="ImageServlet" method="post" enctype="multipart/form-data" 
      class="space-y-6">
    
    <div class="space-y-2">
        <label class="block text-sm font-semibold text-gray-700 mb-2">
            <span class="flex items-center gap-2">
                <svg><!-- Upload icon --></svg>
                Select Images
            </span>
        </label>
        
        <input type="file" name="image" accept="image/*" 
               multiple required id="imageInput"
               class="w-full px-4 py-3 border-2 border-gray-200 
                      rounded-xl focus:border-blue-500 
                      focus:ring-2 focus:ring-blue-200 
                      transition-all duration-200 
                      hover:border-gray-300 cursor-pointer 
                      file:mr-4 file:py-2 file:px-4 
                      file:rounded-lg file:border-0 
                      file:text-sm file:font-semibold 
                      file:bg-blue-50 file:text-blue-700 
                      hover:file:bg-blue-100">
        
        <p class="text-xs text-gray-500 mt-1">
            Maximum file size: 50 MB per file. You can select multiple images.
        </p>
        
        <div id="fileInfo" class="text-xs text-blue-600 mt-2 hidden"></div>
    </div>
    
    <div class="pt-2">
        <button type="submit" 
                class="w-full bg-gradient-to-r from-blue-600 to-purple-600 
                       text-white font-semibold py-3.5 rounded-xl 
                       shadow-lg hover:shadow-xl 
                       transform hover:-translate-y-0.5 
                       transition-all duration-200 
                       flex items-center justify-center gap-2">
            <svg><!-- Lightning icon --></svg>
            Convert to PDF
        </button>
    </div>
</form>
```

**Features:**
- Multiple file selection
- Image format filtering (accept="image/*")
- Required field validation
- Styled file input button
- Gradient submit button
- Hover effects

**4. Client-side Validation Script (lines 67-130)**

```javascript
document.addEventListener('DOMContentLoaded', function() {
    const imageInput = document.getElementById('imageInput');
    const fileInfo = document.getElementById('fileInfo');
    const maxSize = 50 * 1024 * 1024; // 50 MB
    
    imageInput.addEventListener('change', function(e) {
        const files = e.target.files;
        let hasError = false;
        let errorMessage = '';
        let validFiles = [];
        let totalSize = 0;
        
        if (files.length === 0) {
            fileInfo.classList.add('hidden');
            return;
        }
        
        // Validate each file
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            totalSize += file.size;
            
            // Check file type
            if (!file.type.startsWith('image/')) {
                errorMessage += file.name + ' is not an image file. ';
                hasError = true;
                continue;
            }
            
            // Check file size
            if (file.size > maxSize) {
                errorMessage += file.name + ' exceeds 50 MB limit. ';
                hasError = true;
                continue;
            }
            
            validFiles.push(file);
        }
        
        // Show error if any files are invalid
        if (hasError) {
            alert('Some files were invalid:\n' + errorMessage + 
                  '\nPlease select valid image files.');
            e.target.value = '';
            fileInfo.classList.add('hidden');
            return;
        }
        
        // Show file count and total size
        const fileCount = validFiles.length;
        const totalSizeMB = (totalSize / (1024 * 1024)).toFixed(2);
        
        if (fileCount > 0) {
            fileInfo.textContent = fileCount === 1 
                ? '1 image selected (' + totalSizeMB + ' MB)'
                : fileCount + ' images selected (' + totalSizeMB + ' MB total)';
            fileInfo.classList.remove('hidden');
        } else {
            fileInfo.classList.add('hidden');
        }
    });
});
```

**Validation Features:**
- Real-time file validation
- MIME type checking
- Size limit checking (50MB per file)
- Total size calculation
- User-friendly error messages
- File info display (count + total size)

#### Color Palette

| Element | Colors |
|---------|--------|
| Background | `from-blue-50 via-indigo-50 to-purple-50` |
| Icon background | `from-blue-500 to-purple-600` |
| Title text | `from-blue-600 to-purple-600` |
| Button background | `from-blue-600 to-purple-600` |
| Error banner | `bg-red-50 border-red-200 text-red-700` |
| File button | `bg-blue-50 text-blue-700 hover:bg-blue-100` |

#### Responsive Design

```css
/* Mobile-first approach */
body {
    padding: 1rem; /* p-4 */
}

.container {
    width: 100%;
    max-width: 32rem; /* max-w-lg */
}

/* Automatically responsive with Tailwind */
```

---

### 2. result.jsp - Success Page

**Path:** `src/main/webapp/result.jsp`  
**Lines:** 50  
**Purpose:** Display success and download link

#### Visual Design

```
┌─────────────────────────────────────────────┐
│    [Gradient Background]                    │
│                                             │
│   ┌──────────────────────────────────┐     │
│   │                                  │     │
│   │        ✓ (animated)              │     │
│   │                                  │     │
│   │  File Generated Successfully!    │     │
│   │  Your file is ready to download  │     │
│   │                                  │     │
│   │  [📥 Download File (button)]     │     │
│   │                                  │     │
│   │  ← Go Back                       │     │
│   │                                  │     │
│   └──────────────────────────────────┘     │
│                                             │
└─────────────────────────────────────────────┘
```

#### HTML Structure

```html
<!DOCTYPE html>
<html>
<head>
    <title>Result</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center 
             bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4">
    <!-- Content -->
</body>
</html>
```

#### Key Components

**1. Success Icon (lines 14-19)**

```html
<div class="mb-6">
    <div class="inline-flex items-center justify-center 
                w-20 h-20 
                bg-gradient-to-br from-green-400 to-emerald-600 
                rounded-full mb-4 shadow-lg animate-pulse">
        <svg class="w-10 h-10 text-white" fill="none" 
             stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" 
                  stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
    </div>
    
    <h2 class="text-2xl font-bold 
               bg-gradient-to-r from-green-600 to-emerald-600 
               bg-clip-text text-transparent mb-2">
        File Generated Successfully!
    </h2>
    
    <p class="text-gray-500 text-sm">
        Your file is ready to download
    </p>
</div>
```

**Features:**
- Animated checkmark (pulse effect)
- Green gradient theme
- Clear success messaging

**2. Download Button (lines 26-34)**

```html
<a href="download?file=<%= java.net.URLEncoder.encode(
                         (String)request.getAttribute("result"), "UTF-8") %>"
   class="inline-flex items-center justify-center gap-2 
          w-full bg-gradient-to-r from-blue-600 to-purple-600 
          text-white font-semibold py-3.5 px-6 rounded-xl 
          shadow-lg hover:shadow-xl 
          transform hover:-translate-y-0.5 
          transition-all duration-200"
   download>
    <svg class="w-5 h-5" fill="none" stroke="currentColor" 
         viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" 
              stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4-4m0 0L8 8m4-4v12"></path>
    </svg>
    Download File
</a>
```

**Features:**
- URL-encoded filename
- Download attribute for immediate save
- Gradient button matching upload page
- Hover effects

**3. Back Link (lines 36-44)**

```html
<div class="pt-2">
    <a href="index.jsp" 
       class="inline-flex items-center justify-center gap-2 
              text-gray-600 hover:text-gray-900 
              font-medium transition-colors duration-200 text-sm">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" 
             viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" 
                  stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
        </svg>
        Go Back
    </a>
</div>
```

**Features:**
- Returns to upload page
- Subtle styling
- Left arrow icon

#### Dynamic Content

**Result Attribute:**

```jsp
<%= request.getAttribute("result") %>
```

Set by `ImageServlet`:
```java
req.setAttribute("result", "output_1703516789123.pdf");
```

**URL Encoding:**

```jsp
<%= java.net.URLEncoder.encode(
    (String)request.getAttribute("result"), "UTF-8"
) %>
```

Ensures special characters in filename are properly encoded.

---

## CSS Framework - Tailwind

### Usage Pattern

**Tailwind is loaded from CDN:**

```html
<script src="https://cdn.tailwindcss.com"></script>
```

### Key Utility Classes Used

| Class | Purpose |
|-------|---------|
| `bg-gradient-to-br` | Gradient background |
| `rounded-xl` | Large border radius |
| `shadow-lg` | Large drop shadow |
| `hover:shadow-xl` | Shadow on hover |
| `transform hover:-translate-y-0.5` | Lift effect on hover |
| `transition-all duration-200` | Smooth transitions |
| `backdrop-blur-sm` | Glassmorphism effect |
| `bg-white/90` | Semi-transparent background |

### Design Tokens

**Spacing:**
- Padding: `p-4`, `p-8`
- Margin: `mb-2`, `mb-4`, `mb-6`, `mb-8`
- Gap: `gap-2`, `gap-3`

**Typography:**
- Font sizes: `text-sm`, `text-2xl`, `text-3xl`
- Font weights: `font-medium`, `font-semibold`, `font-bold`

**Colors:**
- Blue: `blue-50`, `blue-500`, `blue-600`, `blue-700`
- Purple: `purple-50`, `purple-600`
- Indigo: `indigo-50`
- Green: `green-400`, `green-600`
- Emerald: `emerald-600`
- Red: `red-50`, `red-200`, `red-600`, `red-700`
- Gray: `gray-200`, `gray-500`, `gray-600`, `gray-700`, `gray-900`

---

## Icons

**Source:** Inline SVG (not icon library)

**All icons are custom SVG paths with Tailwind styling:**

```html
<svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" 
     viewBox="0 0 24 24">
    <path stroke-linecap="round" stroke-linejoin="round" 
          stroke-width="2" d="..."></path>
</svg>
```

**Icons Used:**

| Page | Icon | Purpose |
|------|------|---------|
| index.jsp | Image | Header icon |
| index.jsp | Cloud upload | File input label |
| index.jsp | Alert circle | Error icon |
| index.jsp | Lightning bolt | Submit button |
| result.jsp | Checkmark | Success icon |
| result.jsp | Download | Download button |
| result.jsp | Left arrow | Back link |

---

## Typography

**Font Family:** Inter (Google Fonts)

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
body { font-family: 'Inter', sans-serif; }
```

**Weights Used:**
- 400 (Regular) - Body text
- 500 (Medium) - Labels, links
- 600 (Semi-bold) - Buttons
- 700 (Bold) - Headings

---

## Accessibility

### Current State

✅ **Implemented:**
- Semantic HTML (`<h1>`, `<p>`, `<label>`, etc.)
- Required field indicator
- Error messages
- Focus states on inputs
- Descriptive link text

⚠️ **Missing:**
- ARIA labels
- Keyboard navigation highlights
- Screen reader announcements
- High contrast mode support
- Form validation announcements

### Recommendations

```html
<!-- Add ARIA labels -->
<input type="file" 
       name="image" 
       aria-label="Select images to convert" 
       aria-required="true">

<!-- Add live region for errors -->
<div role="alert" aria-live="polite">
    <%= error %>
</div>

<!-- Add focus-visible for keyboard users -->
<style>
.focus-visible:focus {
    outline: 3px solid blue;
}
</style>
```

---

## Browser Compatibility

**Tailwind CSS:** Supports all modern browsers

**Features Used:**
- CSS Grid / Flexbox ✅
- CSS Gradients ✅
- SVG ✅
- File API ✅
- FormData ✅

**Minimum Browser Versions:**
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

---

## Performance

### Optimization Strategies

**CSS:**
- Single CSS framework (Tailwind CDN)
- No custom CSS files
- Utility-first approach (small HTML footprint)

**JavaScript:**
- Minimal vanilla JS
- No framework overhead
- Event listeners on DOMContentLoaded

**Images/Icons:**
- SVG icons (scalable, small filesize)
- No external icon library

### Load Time

Typical page load:
1. HTML: < 5KB
2. Tailwind CSS CDN: ~50KB (gzipped)
3. Google Fonts: ~10KB
4. Total: **~65KB**

---

## Future Enhancements

### Potential Improvements

1. **Progress Indicator**
   ```html
   <div class="progress-bar" id="uploadProgress">
       <div class="progress-fill"></div>
   </div>
   ```

2. **Drag & Drop**
   ```javascript
   dropZone.addEventListener('drop', handleDrop);
   ```

3. **Image Preview**
   ```html
   <div class="image-preview-grid">
       <!-- Thumbnail for each uploaded image -->
   </div>
   ```

4. **Dark Mode**
   ```html
   <html class="dark">
   <!-- Use dark: prefix for dark mode styles -->
   ```

5. **Loading Spinner**
   ```html
   <div class="spinner" id="loadingSpinner">
       <!-- Animated spinner during conversion -->
   </div>
   ```

---

**Next:** [Dependencies Documentation](./dependencies.md)
