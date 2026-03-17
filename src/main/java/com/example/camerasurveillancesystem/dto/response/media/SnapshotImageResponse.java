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
public class SnapshotImageResponse {

    private Long id;
    private Long cameraId;
    private String cameraName;
    private Long aiEventId;
    private Long alertId;
    private String filePath;
    private String imageUrl; // URL để xem/download ảnh
    private String thumbnailUrl; // URL thumbnail nhỏ
    private LocalDateTime capturedAt;
    private Long fileSizeBytes;
    private String fileSizeKB; // Formatted: "256 KB"
    private Integer width;
    private Integer height;
    private String format;
    private String triggerType;
    private String description;
    private String tags;
    private String metadata;
    private LocalDateTime createdAt;
}
