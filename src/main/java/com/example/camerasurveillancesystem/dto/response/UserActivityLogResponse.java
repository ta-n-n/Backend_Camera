package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLogResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private String httpMethod;
    private String requestUrl;
    private Integer responseStatus;
    private Long executionTime;
    private String oldValue;
    private String newValue;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
