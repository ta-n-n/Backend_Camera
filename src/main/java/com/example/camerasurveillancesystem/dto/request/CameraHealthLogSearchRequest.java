package com.example.camerasurveillancesystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraHealthLogSearchRequest {

    private Long cameraId;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String errorCode;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 50;
    private String sortBy = "checkedAt";
    private String sortDirection = "DESC";
}
