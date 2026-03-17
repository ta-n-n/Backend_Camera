package com.example.camerasurveillancesystem.dto.request.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertSearchRequest {

    private Long cameraId;
    private String alertType;
    private String severity;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long assignedToUserId;
    
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
