package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.dto.response.BackupResponse;
import com.example.camerasurveillancesystem.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.directory:backups}")
    private String backupDirectory;

    @Value("${backup.files.directory:uploads}")
    private String filesDirectory;

    @Override
    public BackupResponse backupDatabase() {
        log.info("Starting database backup...");
        long startTime = System.currentTimeMillis();
        
        String backupId = generateBackupId("DB");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = String.format("db_backup_%s.sql", timestamp);
        Path backupPath = Paths.get(backupDirectory, backupFileName);

        try {
            // Create backup directory if not exists
            Files.createDirectories(Paths.get(backupDirectory));

            // Extract database name from URL
            String dbName = extractDatabaseName(dbUrl);

            // Build mysqldump command
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-h", extractHost(dbUrl),
                    "-P", extractPort(dbUrl),
                    "-u", dbUsername,
                    "-p" + dbPassword,
                    dbName,
                    "--result-file=" + backupPath.toAbsolutePath()
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                long fileSize = Files.size(backupPath);
                log.info("Database backup completed successfully: {} ({}MB)", backupFileName, fileSize / 1024 / 1024);

                return BackupResponse.builder()
                        .backupId(backupId)
                        .backupType("DATABASE")
                        .status("COMPLETED")
                        .filePath(backupPath.toString())
                        .fileSize(fileSize)
                        .createdAt(LocalDateTime.now())
                        .duration(duration)
                        .build();
            } else {
                log.error("Database backup failed with exit code: {}", exitCode);
                return BackupResponse.builder()
                        .backupId(backupId)
                        .backupType("DATABASE")
                        .status("FAILED")
                        .createdAt(LocalDateTime.now())
                        .duration(duration)
                        .errorMessage("Backup process failed with exit code: " + exitCode)
                        .build();
            }

        } catch (Exception e) {
            log.error("Database backup failed", e);
            return BackupResponse.builder()
                    .backupId(backupId)
                    .backupType("DATABASE")
                    .status("FAILED")
                    .createdAt(LocalDateTime.now())
                    .duration(System.currentTimeMillis() - startTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public BackupResponse backupFiles() {
        log.info("Starting files backup...");
        long startTime = System.currentTimeMillis();

        String backupId = generateBackupId("FILES");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = String.format("files_backup_%s.zip", timestamp);
        Path backupPath = Paths.get(backupDirectory, backupFileName);

        try {
            Files.createDirectories(Paths.get(backupDirectory));

            // Use zip command to compress files directory
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell",
                    "-Command",
                    String.format("Compress-Archive -Path '%s\\*' -DestinationPath '%s' -Force",
                            filesDirectory, backupPath.toAbsolutePath())
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0 && Files.exists(backupPath)) {
                long fileSize = Files.size(backupPath);
                log.info("Files backup completed successfully: {} ({}MB)", backupFileName, fileSize / 1024 / 1024);

                return BackupResponse.builder()
                        .backupId(backupId)
                        .backupType("FILES")
                        .status("COMPLETED")
                        .filePath(backupPath.toString())
                        .fileSize(fileSize)
                        .createdAt(LocalDateTime.now())
                        .duration(duration)
                        .build();
            } else {
                return BackupResponse.builder()
                        .backupId(backupId)
                        .backupType("FILES")
                        .status("FAILED")
                        .createdAt(LocalDateTime.now())
                        .duration(duration)
                        .errorMessage("Backup process failed")
                        .build();
            }

        } catch (Exception e) {
            log.error("Files backup failed", e);
            return BackupResponse.builder()
                    .backupId(backupId)
                    .backupType("FILES")
                    .status("FAILED")
                    .createdAt(LocalDateTime.now())
                    .duration(System.currentTimeMillis() - startTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public BackupResponse fullBackup() {
        log.info("Starting full backup (database + files)...");
        long startTime = System.currentTimeMillis();

        BackupResponse dbBackup = backupDatabase();
        BackupResponse filesBackup = backupFiles();

        String backupId = generateBackupId("FULL");
        boolean success = "COMPLETED".equals(dbBackup.getStatus()) && "COMPLETED".equals(filesBackup.getStatus());

        return BackupResponse.builder()
                .backupId(backupId)
                .backupType("FULL")
                .status(success ? "COMPLETED" : "FAILED")
                .createdAt(LocalDateTime.now())
                .duration(System.currentTimeMillis() - startTime)
                .errorMessage(success ? null : "One or more backup components failed")
                .build();
    }

    @Override
    public void restoreDatabase(String backupId) {
        log.info("Restoring database from backup: {}", backupId);
        
        File[] backupFiles = new File(backupDirectory).listFiles((dir, name) -> name.startsWith("db_backup_"));
        
        if (backupFiles == null || backupFiles.length == 0) {
            throw new RuntimeException("No database backup found");
        }

        // Find the specific backup file or use the latest
        File backupFile = Arrays.stream(backupFiles)
                .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .orElseThrow(() -> new RuntimeException("Backup file not found"));

        try {
            String dbName = extractDatabaseName(dbUrl);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysql",
                    "-h", extractHost(dbUrl),
                    "-P", extractPort(dbUrl),
                    "-u", dbUsername,
                    "-p" + dbPassword,
                    dbName,
                    "-e", "source " + backupFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Database restore failed with exit code: " + exitCode);
            }

            log.info("Database restored successfully from: {}", backupFile.getName());

        } catch (Exception e) {
            log.error("Database restore failed", e);
            throw new RuntimeException("Database restore failed: " + e.getMessage());
        }
    }

    @Override
    public void restoreFiles(String backupId) {
        log.info("Restoring files from backup: {}", backupId);

        File[] backupFiles = new File(backupDirectory).listFiles((dir, name) -> name.startsWith("files_backup_"));

        if (backupFiles == null || backupFiles.length == 0) {
            throw new RuntimeException("No files backup found");
        }

        File backupFile = Arrays.stream(backupFiles)
                .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .orElseThrow(() -> new RuntimeException("Backup file not found"));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell",
                    "-Command",
                    String.format("Expand-Archive -Path '%s' -DestinationPath '%s' -Force",
                            backupFile.getAbsolutePath(), filesDirectory)
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Files restore failed");
            }

            log.info("Files restored successfully from: {}", backupFile.getName());

        } catch (Exception e) {
            log.error("Files restore failed", e);
            throw new RuntimeException("Files restore failed: " + e.getMessage());
        }
    }

    @Override
    public List<BackupResponse> getAllBackups() {
        List<BackupResponse> backups = new ArrayList<>();

        File backupDir = new File(backupDirectory);
        if (!backupDir.exists()) {
            return backups;
        }

        File[] files = backupDir.listFiles();
        if (files != null) {
            return Arrays.stream(files)
                    .filter(File::isFile)
                    .map(this::fileToBackupResponse)
                    .collect(Collectors.toList());
        }

        return backups;
    }

    @Override
    public void deleteOldBackups(int daysToKeep) {
        log.info("Deleting backups older than {} days", daysToKeep);

        File backupDir = new File(backupDirectory);
        if (!backupDir.exists()) {
            return;
        }

        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);

        File[] files = backupDir.listFiles();
        if (files != null) {
            int deletedCount = 0;
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                        log.info("Deleted old backup: {}", file.getName());
                    }
                }
            }
            log.info("Deleted {} old backup files", deletedCount);
        }
    }

    @Override
    public BackupResponse getBackupById(String backupId) {
        return getAllBackups().stream()
                .filter(b -> b.getBackupId().equals(backupId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Backup not found: " + backupId));
    }

    private String generateBackupId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private BackupResponse fileToBackupResponse(File file) {
        String fileName = file.getName();
        String type = fileName.startsWith("db_") ? "DATABASE" :
                     fileName.startsWith("files_") ? "FILES" : "UNKNOWN";

        return BackupResponse.builder()
                .backupId(fileName)
                .backupType(type)
                .status("COMPLETED")
                .filePath(file.getAbsolutePath())
                .fileSize(file.length())
                .createdAt(LocalDateTime.ofInstant(
                        new java.util.Date(file.lastModified()).toInstant(),
                        java.time.ZoneId.systemDefault()))
                .build();
    }

    private String extractDatabaseName(String url) {
        // jdbc:mysql://localhost:3306/dbname?params
        String[] parts = url.split("/");
        String dbPart = parts[parts.length - 1];
        return dbPart.split("\\?")[0];
    }

    private String extractHost(String url) {
        // jdbc:mysql://localhost:3306/dbname
        String hostPart = url.split("//")[1].split("/")[0];
        return hostPart.split(":")[0];
    }

    private String extractPort(String url) {
        String hostPart = url.split("//")[1].split("/")[0];
        String[] parts = hostPart.split(":");
        return parts.length > 1 ? parts[1] : "3306";
    }
}
