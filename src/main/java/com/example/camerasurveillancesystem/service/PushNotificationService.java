package com.example.camerasurveillancesystem.service;

import java.util.Map;

public interface PushNotificationService {

    /**
     * Send push notification to device
     */
    void sendToDevice(String deviceToken, String title, String body, Map<String, String> data);

    /**
     * Send push notification to topic
     */
    void sendToTopic(String topic, String title, String body, Map<String, String> data);

    /**
     * Send alert push notification
     */
    void sendAlertNotification(String deviceToken, String alertTitle, String cameraName, String severity);

    /**
     * Send AI detection push notification
     */
    void sendAiDetectionNotification(String deviceToken, String eventType, String cameraName);

    /**
     * Subscribe device to topic
     */
    void subscribeToTopic(String deviceToken, String topic);

    /**
     * Unsubscribe device from topic
     */
    void unsubscribeFromTopic(String deviceToken, String topic);
}
