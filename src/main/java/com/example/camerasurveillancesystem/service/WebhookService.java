package com.example.camerasurveillancesystem.service;

import java.util.Map;

public interface WebhookService {

    /**
     * Send webhook notification
     */
    void sendWebhook(String url, Map<String, Object> payload);

    /**
     * Send alert webhook
     */
    void sendAlertWebhook(String url, Long alertId, String alertTitle, String severity);

    /**
     * Send AI detection webhook
     */
    void sendAiDetectionWebhook(String url, Long eventId, String eventType, Double confidence);

    /**
     * Send camera status webhook
     */
    void sendCameraStatusWebhook(String url, Long cameraId, String status);
}
