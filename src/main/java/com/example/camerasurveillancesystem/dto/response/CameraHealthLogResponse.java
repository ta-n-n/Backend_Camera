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
public class CameraHealthLogResponse {

    private Long id;
    private Long cameraId;
    private String cameraName;
    private String cameraCode;
    private String status;
    private String message;
    private Integer cpuUsage;
    private Integer memoryUsage;
    private Integer diskUsage;
    private Integer temperature;
    private Integer bandwidth;
    private String errorCode;
    private LocalDateTime checkedAt;
}
