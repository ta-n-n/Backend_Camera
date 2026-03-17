package com.example.camerasurveillancesystem.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingJobResponse {

    private Long id;
    private String jobType;
    private String status;
    private String priority;
    private String parameters;
    private String result;
    private String errorMessage;
    private Integer progress;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer retryCount;
    private Integer maxRetries;
    private Long createdByUserId;
    private String createdByUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
