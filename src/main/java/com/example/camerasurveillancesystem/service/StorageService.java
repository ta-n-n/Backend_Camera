package com.example.camerasurveillancesystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    /**
     * Store file and return the file path
     */
    String store(MultipartFile file, String directory) throws IOException;

    /**
     * Store file with custom filename
     */
    String store(MultipartFile file, String directory, String filename) throws IOException;

    /**
     * Store file from InputStream
     */
    String store(InputStream inputStream, String directory, String filename) throws IOException;

    /**
     * Load file as InputStream
     */
    InputStream load(String filePath) throws IOException;

    /**
     * Delete file
     */
    void delete(String filePath) throws IOException;

    /**
     * Check if file exists
     */
    boolean exists(String filePath);

    /**
     * Get file size
     */
    long getSize(String filePath) throws IOException;

    /**
     * Get storage type (LOCAL, S3, AZURE, etc.)
     */
    String getStorageType();
}
