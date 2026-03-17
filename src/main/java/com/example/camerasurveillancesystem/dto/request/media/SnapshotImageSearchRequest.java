package com.example.camerasurveillancesystem.dto.request.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotImageSearchRequest {

    private Long cameraId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String triggerType;
    private String tag; // Search by tag
    private Long aiEventId;
    private Long alertId;
    
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "capturedAt";
    private String sortDirection = "DESC";
}
