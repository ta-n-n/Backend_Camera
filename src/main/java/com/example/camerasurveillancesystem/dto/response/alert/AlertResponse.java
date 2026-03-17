package com.example.camerasurveillancesystem.dto.response.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {

    private Long id;
    private Long cameraId;
    private String cameraName;
    private Long aiEventId;
    private String alertType;
    private String severity;
    private String status;
    private String title;
    private String description;
    private String location;
    private String snapshotUrl;
    private String videoUrl;
    private Long assignedToUserId;
    private String assignedToUserName;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
