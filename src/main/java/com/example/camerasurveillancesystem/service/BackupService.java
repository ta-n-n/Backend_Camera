package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.BackupResponse;

import java.util.List;

public interface BackupService {
    
    /**
     * Backup database
     */
    BackupResponse backupDatabase();
    
    /**
     * Backup files (videos, snapshots, etc.)
     */
    BackupResponse backupFiles();
    
    /**
     * Full backup (database + files)
     */
    BackupResponse fullBackup();
    
    /**
     * Restore database from backup
     */
    void restoreDatabase(String backupId);
    
    /**
     * Restore files from backup
     */
    void restoreFiles(String backupId);
    
    /**
     * Get all backups
     */
    List<BackupResponse> getAllBackups();
    
    /**
     * Delete old backups (older than specified days)
     */
    void deleteOldBackups(int daysToKeep);
    
    /**
     * Get backup by ID
     */
    BackupResponse getBackupById(String backupId);
}
