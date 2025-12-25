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
<body class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4">

<div class="bg-white/90 backdrop-blur-sm p-8 rounded-2xl shadow-2xl w-full max-w-lg border border-white/20 text-center">
    <div class="mb-6">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-green-400 to-emerald-600 rounded-full mb-4 shadow-lg animate-pulse">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
        </div>
        <h2 class="text-2xl font-bold bg-gradient-to-r from-green-600 to-emerald-600 bg-clip-text text-transparent mb-2">
            File Generated Successfully!
        </h2>
        <p class="text-gray-500 text-sm">Your file is ready to download</p>
    </div>

    <div class="space-y-4">
        <a href="download?file=<%= java.net.URLEncoder.encode((String)request.getAttribute("result"), "UTF-8") %>"
           class="inline-flex items-center justify-center gap-2 w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold py-3.5 px-6 rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200"
           download>
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4-4m0 0L8 8m4-4v12"></path>
            </svg>
            Download File
        </a>

        <div class="pt-2">
            <a href="index.jsp" 
               class="inline-flex items-center justify-center gap-2 text-gray-600 hover:text-gray-900 font-medium transition-colors duration-200 text-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                </svg>
                Go Back
            </a>
        </div>
    </div>
</div>

</body>
</html>
