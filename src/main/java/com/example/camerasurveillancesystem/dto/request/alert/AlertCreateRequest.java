package com.example.camerasurveillancesystem.dto.request.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertCreateRequest {

    @NotNull(message = "Camera ID is required")
    private Long cameraId;

    private Long aiEventId; // Optional: có thể tạo alert thủ công

    @NotBlank(message = "Alert type is required")
    private String alertType; // INTRUSION, LOITERING, FIRE, SUSPICIOUS_ACTIVITY, etc.

    @NotBlank(message = "Severity is required")
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String location; // Vị trí cụ thể trong frame

    private String snapshotUrl; // URL ảnh snapshot

    private String videoUrl; // URL video clip

    private String metadata; // JSON metadata bổ sung
}
