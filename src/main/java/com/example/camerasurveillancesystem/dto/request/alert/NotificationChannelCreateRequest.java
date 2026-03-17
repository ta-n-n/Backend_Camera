package com.example.camerasurveillancesystem.dto.request.alert;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChannelCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Channel type is required")
    private String channelType; // EMAIL, SMS, TELEGRAM, WEBHOOK, PUSH_NOTIFICATION

    private String description;

    private Boolean isActive = true;

    private Boolean isDefault = false;

    private Integer priority = 10; // Lower = higher priority

    // Config cho từng loại channel (JSON string)
    private String emailConfig; // {"smtp_host": "...", "smtp_port": ..., "from": "..."}
    private String smsConfig; // {"provider": "twilio", "account_sid": "...", "auth_token": "..."}
    private String telegramConfig; // {"bot_token": "...", "chat_id": "..."}
    private String webhookConfig; // {"url": "...", "method": "POST", "headers": {...}}
    private String pushConfig; // {"fcm_server_key": "...", "topic": "..."}
}
