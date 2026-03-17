package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.provider:twilio}")
    private String provider;

    @Value("${sms.api.key:}")
    private String apiKey;

    @Value("${sms.api.secret:}")
    private String apiSecret;

    @Value("${sms.from.number:}")
    private String fromNumber;

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            log.info("Sending SMS to {}: {}", phoneNumber, message);
            
            // TODO: Integrate with SMS provider (Twilio, AWS SNS, etc.)
            // For now, just log the SMS
            switch (provider.toLowerCase()) {
                case "twilio":
                    sendViaTwilio(phoneNumber, message);
                    break;
                case "aws-sns":
                    sendViaAwsSns(phoneNumber, message);
                    break;
                default:
                    log.warn("SMS provider {} not configured", provider);
            }
            
            log.info("SMS sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    public void sendAlertSms(String phoneNumber, String alertTitle, String cameraName) {
        String message = String.format("Alert: %s from camera %s", alertTitle, cameraName);
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendVerificationCode(String phoneNumber, String code) {
        String message = String.format("Your verification code is: %s", code);
        sendSms(phoneNumber, message);
    }

    private void sendViaTwilio(String phoneNumber, String message) {
        // TODO: Implement Twilio integration
        // Twilio.init(apiKey, apiSecret);
        // Message.creator(
        //     new PhoneNumber(phoneNumber),
        //     new PhoneNumber(fromNumber),
        //     message
        // ).create();
        log.info("Twilio SMS simulation - To: {}, Message: {}", phoneNumber, message);
    }

    private void sendViaAwsSns(String phoneNumber, String message) {
        // TODO: Implement AWS SNS integration
        // AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
        // PublishRequest publishRequest = new PublishRequest()
        //     .withPhoneNumber(phoneNumber)
        //     .withMessage(message);
        // snsClient.publish(publishRequest);
        log.info("AWS SNS SMS simulation - To: {}, Message: {}", phoneNumber, message);
    }
}
