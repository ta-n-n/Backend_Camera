package com.example.camerasurveillancesystem.dto.response.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertLogResponse {

    private Long id;
    private Long alertId;
    private String actionType;
    private Long performedById;
    private String performedByName;
    private String comment;
    private String previousValue;
    private String newValue;
    private LocalDateTime createdAt;
}
