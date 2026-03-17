package com.example.camerasurveillancesystem.service;

import java.util.Map;

public interface EmailService {

    /**
     * Send simple text email
     */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Send HTML email
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Send email with template
     */
    void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables);

    /**
     * Send alert notification email
     */
    void sendAlertEmail(String to, String alertTitle, String cameraName, String severity);

    /**
     * Send AI detection email
     */
    void sendAiDetectionEmail(String to, String eventType, String cameraName, String confidence);
}
