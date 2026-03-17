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
public class AlertNotificationResponse {

    private Long id;
    private Long alertId;
    private Long notificationChannelId;
    private String channelName;
    private String channelType;
    private String recipient;
    private String subject;
    private String message;
    private String status; // PENDING, SENT, FAILED
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime sentAt;
    private String metadata;
}
