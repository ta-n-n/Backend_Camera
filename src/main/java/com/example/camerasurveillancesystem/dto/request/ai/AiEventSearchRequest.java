package com.example.camerasurveillancesystem.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiEventSearchRequest {

    private Long cameraId;
    private Long modelId;
    private String eventType; // PERSON_DETECTED, VEHICLE_DETECTED, INTRUSION, LOITERING, CROWD
    private Double minConfidence;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "detectedAt";
    private String sortDirection = "DESC";
}
