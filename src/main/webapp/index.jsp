<!DOCTYPE html>
<html>
<head>
    <title>Image Utility</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://cdn.jsdelivr.net/npm/heroicons@2.0.18/24/outline/index.js" type="module"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4">

<div class="bg-white/90 backdrop-blur-sm p-8 rounded-2xl shadow-2xl w-full max-w-lg border border-white/20">
    <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl mb-4 shadow-lg">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
            </svg>
        </div>
        <h1 class="text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
            Image to PDF Converter
        </h1>
        <p class="text-gray-500 text-sm">Convert single or multiple images to PDF format</p>
    </div>

    <% String error = request.getParameter("error"); %>
    <% if (error != null && !error.isEmpty()) { %>
    <div class="mb-6 p-4 bg-red-50 border-2 border-red-200 rounded-xl">
        <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <p class="text-sm text-red-700 font-medium"><%= error %></p>
        </div>
    </div>
    <% } %>

    <form action="ImageServlet" method="post" enctype="multipart/form-data" class="space-y-6">

        <div class="space-y-2">
            <label class="block text-sm font-semibold text-gray-700 mb-2">
                <span class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
                    </svg>
                    Select Images
                </span>
            </label>
            <input type="file" name="image" accept="image/*" multiple required id="imageInput"
                   class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all duration-200 hover:border-gray-300 cursor-pointer file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100">
            <p class="text-xs text-gray-500 mt-1">Maximum file size: 50 MB per file. You can select multiple images.</p>
            <div id="fileInfo" class="text-xs text-blue-600 mt-2 hidden"></div>
        </div>

        <div class="pt-2">
            <button type="submit" class="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold py-3.5 rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200 flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                </svg>
                Convert to PDF
            </button>
        </div>
    </form>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // File size validation for multiple files
        const imageInput = document.getElementById('imageInput');
        const fileInfo = document.getElementById('fileInfo');
        const maxSize = 50 * 1024 * 1024; // 50 MB per file
        
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
                alert('Some files were invalid:\n' + errorMessage + '\nPlease select valid image files.');
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
</script>

</body>
</html>
