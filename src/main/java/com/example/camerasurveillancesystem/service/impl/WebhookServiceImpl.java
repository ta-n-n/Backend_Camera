package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendWebhook(String url, Map<String, Object> payload) {
        try {
            log.info("Sending webhook to: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook sent successfully to: {}", url);
            } else {
                log.warn("Webhook returned non-2xx status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send webhook to {}: {}", url, e.getMessage());
            // Don't throw exception - webhooks should not break the main flow
        }
    }

    @Override
    public void sendAlertWebhook(String url, Long alertId, String alertTitle, String severity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "alert");
        payload.put("alertId", alertId);
        payload.put("title", alertTitle);
        payload.put("severity", severity);
        payload.put("timestamp", LocalDateTime.now().toString());

        sendWebhook(url, payload);
    }

    @Override
    public void sendAiDetectionWebhook(String url, Long eventId, String eventType, Double confidence) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ai_detection");
        payload.put("eventId", eventId);
        payload.put("eventType", eventType);
        payload.put("confidence", confidence);
        payload.put("timestamp", LocalDateTime.now().toString());

        sendWebhook(url, payload);
    }

    @Override
    public void sendCameraStatusWebhook(String url, Long cameraId, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "camera_status");
        payload.put("cameraId", cameraId);
        payload.put("status", status);
        payload.put("timestamp", LocalDateTime.now().toString());

        sendWebhook(url, payload);
    }
}
