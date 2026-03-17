package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.BackupResponse;
import com.example.camerasurveillancesystem.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@Tag(name = "Backup & Restore", description = "Sao lưu và khôi phục database và files (Admin only)")
public class BackupController {

    private final BackupService backupService;

    @PostMapping("/database")
    @Operation(summary = "Backup database")
    public ResponseEntity<ApiResponse<BackupResponse>> backupDatabase() {
        BackupResponse response = backupService.backupDatabase();
        return ResponseEntity.ok(ApiResponse.success("Database backup initiated", response));
    }

    @PostMapping("/files")
    @Operation(summary = "Backup files (videos, snapshots, etc.)")
    public ResponseEntity<ApiResponse<BackupResponse>> backupFiles() {
        BackupResponse response = backupService.backupFiles();
        return ResponseEntity.ok(ApiResponse.success("Files backup initiated", response));
    }

    @PostMapping("/full")
    @Operation(summary = "Full backup (database + files)")
    public ResponseEntity<ApiResponse<BackupResponse>> fullBackup() {
        BackupResponse response = backupService.fullBackup();
        return ResponseEntity.ok(ApiResponse.success("Full backup initiated", response));
    }

    @PostMapping("/restore/database/{backupId}")
    @Operation(summary = "Restore database from backup")
    public ResponseEntity<ApiResponse<Void>> restoreDatabase(
            @Parameter(description = "Backup ID") @PathVariable String backupId) {
        backupService.restoreDatabase(backupId);
        return ResponseEntity.ok(ApiResponse.success("Database restore completed", null));
    }

    @PostMapping("/restore/files/{backupId}")
    @Operation(summary = "Restore files from backup")
    public ResponseEntity<ApiResponse<Void>> restoreFiles(
            @Parameter(description = "Backup ID") @PathVariable String backupId) {
        backupService.restoreFiles(backupId);
        return ResponseEntity.ok(ApiResponse.success("Files restore completed", null));
    }

    @GetMapping
    @Operation(summary = "Get all backups")
    public ResponseEntity<ApiResponse<List<BackupResponse>>> getAllBackups() {
        List<BackupResponse> response = backupService.getAllBackups();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{backupId}")
    @Operation(summary = "Get backup by ID")
    public ResponseEntity<ApiResponse<BackupResponse>> getBackupById(
            @Parameter(description = "Backup ID") @PathVariable String backupId) {
        BackupResponse response = backupService.getBackupById(backupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Delete old backups")
    public ResponseEntity<ApiResponse<Void>> deleteOldBackups(
            @Parameter(description = "Keep backups from last N days") 
            @RequestParam(defaultValue = "30") int daysToKeep) {
        backupService.deleteOldBackups(daysToKeep);
        return ResponseEntity.ok(ApiResponse.success("Old backups deleted", null));
    }
}
