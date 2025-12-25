package controller;

import util.ImageUtils;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@SuppressWarnings("serial")
@WebServlet("/ImageServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 5,    // 5 MB
    maxFileSize = 1024 * 1024 * 50,         // 50 MB per file
    maxRequestSize = 1024 * 1024 * 300      // 300 MB total (for multiple files)
)
public class ImageServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            // Get all parts from the request
            java.util.Collection<Part> parts = req.getParts();
            java.util.List<String> imagePaths = new java.util.ArrayList<>();
            
            String uploadPath = getServletContext().getRealPath("/") + "uploads/";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            
            // Process all image parts
            for (Part part : parts) {
                // Only process parts with name "image" and that have content
                if ("image".equals(part.getName()) && part.getSize() > 0) {
                    String fileName = System.currentTimeMillis() + "_" + part.getSubmittedFileName();
                    String filePath = uploadPath + fileName;
                    
                    // Validate file type
                    String contentType = part.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        continue; // Skip non-image files
                    }
                    
                    // Save the file
                    part.write(filePath);
                    imagePaths.add(filePath);
                }
            }
            
            // Check if we have at least one image
            if (imagePaths.isEmpty()) {
                res.sendRedirect("index.jsp?error=" + java.net.URLEncoder.encode("No valid image files were uploaded. Please select at least one image file.", "UTF-8"));
                return;
            }

            // Convert all images to a single PDF
            String resultFile;
            try {
                String[] imgPathsArray = imagePaths.toArray(new String[0]);
                resultFile = ImageUtils.imagesToPdf(
                        imgPathsArray,
                        uploadPath + "output_" + System.currentTimeMillis() + ".pdf"
                );
            } catch (Exception e) {
                // Clean up uploaded files on error
                for (String imgPath : imagePaths) {
                    try {
                        new File(imgPath).delete();
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                }
                res.sendRedirect("index.jsp?error=" + java.net.URLEncoder.encode("Error converting images to PDF: " + e.getMessage(), "UTF-8"));
                return;
            }
            
            // Clean up temporary image files after PDF creation
            for (String imgPath : imagePaths) {
                try {
                    new File(imgPath).delete();
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }

            // Set the result file name for download
            String resultFileName = new File(resultFile).getName();
            req.setAttribute("result", resultFileName);
            req.getRequestDispatcher("result.jsp").forward(req, res);
            
        } catch (IllegalStateException e) {
            // Handle file size limit exceeded
            String errorMsg = "File size limit exceeded. ";
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                if (e.getCause().getMessage().contains("FileSizeLimitExceededException")) {
                    errorMsg += "Maximum file size is 50 MB. Please choose a smaller image.";
                } else {
                    errorMsg += e.getCause().getMessage();
                }
            } else {
                errorMsg += "Maximum file size is 50 MB. Please choose a smaller image.";
            }
            res.sendRedirect("index.jsp?error=" + java.net.URLEncoder.encode(errorMsg, "UTF-8"));
        } catch (Exception e) {
            res.sendRedirect("index.jsp?error=" + java.net.URLEncoder.encode("An error occurred: " + e.getMessage(), "UTF-8"));
        }
    }
}
