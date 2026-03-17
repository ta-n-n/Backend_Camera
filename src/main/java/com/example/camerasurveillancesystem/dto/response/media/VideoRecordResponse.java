package com.example.camerasurveillancesystem.dto.response.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoRecordResponse {

    private Long id;
    private Long cameraId;
    private String cameraName;
    private Long aiEventId;
    private String filePath;
    private String downloadUrl; // URL để download/stream
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private String fileSizeMB; // Formatted: "125.5 MB"
    private String resolution;
    private Integer fps;
    private String codec;
    private String recordingType;
    private String metadata;
    private LocalDateTime createdAt;
}
