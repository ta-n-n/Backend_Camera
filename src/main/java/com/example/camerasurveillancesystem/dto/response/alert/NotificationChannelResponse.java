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
public class NotificationChannelResponse {

    private Long id;
    private String name;
    private String channelType;
    private String description;
    private Boolean isActive;
    private Boolean isDefault;
    private Boolean isVerified;
    private Integer priority;
    private String emailConfig;
    private String smsConfig;
    private String telegramConfig;
    private String webhookConfig;
    private String pushConfig;
    private LocalDateTime lastTestedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
