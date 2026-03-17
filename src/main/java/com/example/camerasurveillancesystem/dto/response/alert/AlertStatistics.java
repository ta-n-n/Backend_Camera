package com.example.camerasurveillancesystem.dto.response.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertStatistics {

    private Long totalAlerts;
    private Long newAlerts;
    private Long acknowledgedAlerts;
    private Long resolvedAlerts;
    private Long falsePositiveAlerts;
    
    private Long criticalAlerts;
    private Long highAlerts;
    private Long mediumAlerts;
    private Long lowAlerts;
    
    private Map<String, Long> alertsByType;
    private Map<String, Long> alertsByCamera;
}
