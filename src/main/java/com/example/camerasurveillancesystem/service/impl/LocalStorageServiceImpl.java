package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.base-path:./storage}")
    private String basePath;

    @Override
    public String store(MultipartFile file, String directory) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID().toString() + "_" + originalFilename;
        return store(file, directory, filename);
    }

    @Override
    public String store(MultipartFile file, String directory, String filename) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return store(inputStream, directory, filename);
        }
    }

    @Override
    public String store(InputStream inputStream, String directory, String filename) throws IOException {
        Path directoryPath = Paths.get(basePath, directory);
        Files.createDirectories(directoryPath);

        Path filePath = directoryPath.resolve(filename);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = directory + "/" + filename;
        log.info("File stored successfully: {}", relativePath);
        return relativePath;
    }

    @Override
    public InputStream load(String filePath) throws IOException {
        Path path = Paths.get(basePath, filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return Files.newInputStream(path);
    }

    @Override
    public void delete(String filePath) throws IOException {
        Path path = Paths.get(basePath, filePath);
        Files.deleteIfExists(path);
        log.info("File deleted: {}", filePath);
    }

    @Override
    public boolean exists(String filePath) {
        Path path = Paths.get(basePath, filePath);
        return Files.exists(path);
    }

    @Override
    public long getSize(String filePath) throws IOException {
        Path path = Paths.get(basePath, filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return Files.size(path);
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }
}
