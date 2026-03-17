package com.example.camerasurveillancesystem.ai.detector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tự động download YOLOv8 model
 */
@Component
@Slf4j
public class ModelDownloader {

    // Backup URLs - thử lần lượt
    private static final String[] YOLOV8N_URLS = {
        "https://github.com/ultralytics/assets/releases/download/v0.0.0/yolov8n.onnx",
        "https://storage.googleapis.com/deepsparse/yolov8/yolov8n.onnx"
    };

    /**
     * Download model nếu chưa tồn tại - thử nhiều nguồn
     */
    public boolean downloadModelIfNotExists(String modelPath, String modelSize) {
        Path path = Paths.get(modelPath);
        
        if (Files.exists(path)) {
            log.info("Model already exists: {}", modelPath);
            return true;
        }

        log.info("Model not found. Trying to download...");
        
        try {
            // Tạo thư mục nếu chưa có
            Files.createDirectories(path.getParent());
            
            // Thử download từ nhiều nguồn
            for (int i = 0; i < YOLOV8N_URLS.length; i++) {
                String url = YOLOV8N_URLS[i];
                try {
                    log.info("Trying source {}: {}", i + 1, url);
                    downloadFile(url, modelPath);
                    log.info("✅ Model downloaded successfully!");
                    return true;
                } catch (Exception e) {
                    log.warn("Source {} failed: {}", i + 1, e.getMessage());
                    if (i == YOLOV8N_URLS.length - 1) {
                        throw e; // Nguồn cuối cùng cũng fail
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("❌ All download sources failed");
            return false;
        }
        
        return false;
    }

    private void downloadFile(String urlString, String destinationPath) throws IOException {
        URL url = new URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        
        // Set headers để tránh bị block
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        
        // Follow redirects
        int responseCode = connection.getResponseCode();
        if (responseCode == java.net.HttpURLConnection.HTTP_MOVED_TEMP || 
            responseCode == java.net.HttpURLConnection.HTTP_MOVED_PERM ||
            responseCode == java.net.HttpURLConnection.HTTP_SEE_OTHER) {
            
            String newUrl = connection.getHeaderField("Location");
            log.info("Following redirect to: {}", newUrl);
            connection = (java.net.HttpURLConnection) new URL(newUrl).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        }
        
        try (InputStream in = connection.getInputStream();
             BufferedInputStream bufferedIn = new BufferedInputStream(in);
             FileOutputStream fileOut = new FileOutputStream(destinationPath);
             BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            long lastLogTime = System.currentTimeMillis();
            
            while ((bytesRead = bufferedIn.read(buffer)) != -1) {
                bufferedOut.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                
                // Log progress mỗi 2 giây
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLogTime > 2000) {
                    log.info("Downloaded: {} MB", String.format("%.2f", totalBytes / 1024.0 / 1024.0));
                    lastLogTime = currentTime;
                }
            }
            
            log.info("Total downloaded: {} MB", String.format("%.2f", totalBytes / 1024.0 / 1024.0));
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Download model với custom URL
     */
    public boolean downloadModel(String url, String destinationPath) {
        try {
            Path path = Paths.get(destinationPath);
            Files.createDirectories(path.getParent());
            
            log.info("Downloading from custom URL: {}", url);
            downloadFile(url, destinationPath);
            
            log.info("✅ Model downloaded successfully");
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to download model from custom URL", e);
            return false;
        }
    }
}
