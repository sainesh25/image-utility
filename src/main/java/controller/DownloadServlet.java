package controller;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@SuppressWarnings("serial")
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        String fileName = req.getParameter("file");
        if (fileName == null || fileName.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "File parameter is required");
            return;
        }
        
        // Security: prevent directory traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid file path");
            return;
        }
        
        String uploadPath = getServletContext().getRealPath("/") + "uploads/";
        File file = new File(uploadPath + fileName);
        
        if (!file.exists() || !file.isFile()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }
        
        // Determine content type based on file extension
        String contentType = "application/octet-stream";
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (fileExtension) {
            case "jpg":
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "webp":
                contentType = "image/webp";
                break;
            case "pdf":
                contentType = "application/pdf";
                break;
        }
        
        // Set response headers
        res.setContentType(contentType);
        res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        res.setContentLengthLong(file.length());
        
        // Stream the file
        try (InputStream in = new FileInputStream(file);
             OutputStream out = res.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}

