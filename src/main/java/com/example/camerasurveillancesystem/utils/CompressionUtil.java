package com.example.camerasurveillancesystem.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class CompressionUtil {

    /**
     * Compress file using GZIP
     */
    public static byte[] compressGzip(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }

    /**
     * Decompress GZIP file
     */
    public static byte[] decompressGzip(byte[] compressedData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }

    /**
     * Create ZIP archive from files
     */
    public static void createZipArchive(String zipFilePath, String... sourceFiles) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (String sourceFile : sourceFiles) {
                File fileToZip = new File(sourceFile);
                if (!fileToZip.exists()) {
                    log.warn("File not found, skipping: {}", sourceFile);
                    continue;
                }
                
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zos.putNextEntry(zipEntry);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    
                    zos.closeEntry();
                }
            }
        }
        log.info("ZIP archive created: {}", zipFilePath);
    }

    /**
     * Calculate compression ratio
     */
    public static double getCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) return 0.0;
        return (1.0 - ((double) compressedSize / originalSize)) * 100.0;
    }
}
