package com.example.camerasurveillancesystem.dto.request.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertNotificationCreateRequest {

    @NotNull(message = "Alert ID is required")
    private Long alertId;

    @NotNull(message = "Notification channel ID is required")
    private Long notificationChannelId;

    @NotBlank(message = "Recipient is required")
    private String recipient; // Email address, phone number, telegram chat ID, etc.

    private String subject; // For email

    @NotBlank(message = "Message is required")
    private String message;

    private String metadata; // Additional data (JSON)
}
