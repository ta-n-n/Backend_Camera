package com.example.camerasurveillancesystem.service;

public interface SmsService {

    /**
     * Send SMS notification
     */
    void sendSms(String phoneNumber, String message);

    /**
     * Send alert SMS
     */
    void sendAlertSms(String phoneNumber, String alertTitle, String cameraName);

    /**
     * Send verification code
     */
    void sendVerificationCode(String phoneNumber, String code);
}
