package com.example.camerasurveillancesystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PtzCommandResponse {

    private Long cameraId;
    private String cameraCode;
    private String command;
    private String onvifUrl;
    private String profileToken;
    private Integer speedPercent;
    private Long durationMs;
    private LocalDateTime executedAt;
}
