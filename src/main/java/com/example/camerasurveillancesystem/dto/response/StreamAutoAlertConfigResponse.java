package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamAutoAlertConfigResponse {

    private Long cameraId;
    private Boolean enabled;
    private Double minConfidence;
    private Integer cooldownSeconds;
    private String alertType;
    private String severity;
    private List<String> objectTypes;
    private LocalDateTime lastAlertAt;
}
