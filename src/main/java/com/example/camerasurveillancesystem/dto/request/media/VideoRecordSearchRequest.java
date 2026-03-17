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
public class VideoRecordSearchRequest {

    private Long cameraId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String recordingType;
    private Integer minDuration; // seconds
    private Long minFileSize; // bytes
    
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "startTime";
    private String sortDirection = "DESC";
}
