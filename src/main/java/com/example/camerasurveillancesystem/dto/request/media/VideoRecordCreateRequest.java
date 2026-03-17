package com.example.camerasurveillancesystem.dto.request.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoRecordCreateRequest {

    @NotNull(message = "Camera ID is required")
    private Long cameraId;

    private Long aiEventId; // Optional: nếu video liên quan đến event

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationSeconds;

    @NotNull(message = "File size is required")
    private Long fileSizeBytes;

    private String resolution; // 1920x1080, 1280x720, etc.

    private Integer fps; // Frames per second

    private String codec; // H.264, H.265, etc.

    @NotBlank(message = "Recording type is required")
    private String recordingType; // CONTINUOUS, EVENT_TRIGGERED, SCHEDULED, MANUAL

    private String metadata; // JSON metadata
}
