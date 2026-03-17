package com.example.camerasurveillancesystem.dto.request.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChannelUpdateRequest {

    private String name;
    private String description;
    private Boolean isActive;
    private Boolean isDefault;
    private Integer priority;
    
    private String emailConfig;
    private String smsConfig;
    private String telegramConfig;
    private String webhookConfig;
    private String pushConfig;
}
