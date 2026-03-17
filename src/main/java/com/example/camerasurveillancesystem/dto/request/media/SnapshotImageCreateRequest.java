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
public class SnapshotImageCreateRequest {

    @NotNull(message = "Camera ID is required")
    private Long cameraId;

    private Long aiEventId; // Optional: nếu snapshot từ AI event

    private Long alertId; // Optional: nếu snapshot từ alert

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotNull(message = "Captured time is required")
    private LocalDateTime capturedAt;

    @NotNull(message = "File size is required")
    private Long fileSizeBytes;

    private Integer width;

    private Integer height;

    private String format; // JPEG, PNG, etc.

    @NotBlank(message = "Trigger type is required")
    private String triggerType; // MANUAL, SCHEDULED, EVENT_TRIGGERED, ALERT_TRIGGERED

    private String description;

    private String tags; // JSON array: ["person", "night", "entrance"]

    private String metadata; // JSON metadata
}
