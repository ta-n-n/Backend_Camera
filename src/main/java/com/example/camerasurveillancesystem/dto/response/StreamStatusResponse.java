package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamStatusResponse {

    private Long cameraId;
    private String cameraName;
    private String rtspUrl;
    private Boolean isRunning;
    private Boolean isHealthy;
    private Boolean autoRestartEnabled;
    private LocalDateTime lastCheckTime;
    private String status; // RUNNING, STOPPED, ERROR
    private String message;
}
