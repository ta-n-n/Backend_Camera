package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupResponse {
    
    private String backupId;
    private String backupType; // DATABASE, FILES, FULL
    private String status; // COMPLETED, FAILED, IN_PROGRESS
    private String filePath;
    private Long fileSize;
    private LocalDateTime createdAt;
    private Long duration; // milliseconds
    private String errorMessage;
}
