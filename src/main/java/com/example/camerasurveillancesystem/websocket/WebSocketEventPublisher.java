package com.example.camerasurveillancesystem.websocket;

import com.example.camerasurveillancesystem.dto.response.ai.AiEventResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Publish alert notification to WebSocket subscribers
     */
    public void publishAlertNotification(AlertResponse alert) {
        log.info("Publishing alert notification: {}", alert.getId());
        
        // Broadcast to all alert subscribers
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        
        // Send to specific camera subscribers
        if (alert.getCameraId() != null) {
            messagingTemplate.convertAndSend(
                "/topic/camera/" + alert.getCameraId() + "/alerts", 
                alert
            );
        }
    }

    /**
     * Publish AI detection event to WebSocket subscribers
     */
    public void publishAiDetectionEvent(AiEventResponse event) {
        log.info("Publishing AI detection event: {}", event.getId());
        
        // Broadcast to all AI event subscribers
        messagingTemplate.convertAndSend("/topic/ai-events", event);
        
        // Send to specific camera subscribers
        if (event.getCameraId() != null) {
            messagingTemplate.convertAndSend(
                "/topic/camera/" + event.getCameraId() + "/ai-events", 
                event
            );
        }
    }

    /**
     * Publish camera stream update
     */
    public void publishCameraStreamUpdate(Long cameraId, Map<String, Object> streamData) {
        log.debug("Publishing camera stream update for camera: {}", cameraId);
        messagingTemplate.convertAndSend(
            "/topic/camera/" + cameraId + "/stream", 
            streamData
        );
    }

    /**
     * Publish system status update
     */
    public void publishSystemStatusUpdate(Map<String, Object> status) {
        log.info("Publishing system status update");
        messagingTemplate.convertAndSend("/topic/system/status", status);
    }

    /**
     * Publish camera status update (online/offline)
     */
    public void publishCameraStatusUpdate(Long cameraId, String status) {
        log.info("Publishing camera {} status: {}", cameraId, status);
        messagingTemplate.convertAndSend(
            "/topic/camera/" + cameraId + "/status", 
            Map.of(
                "cameraId", cameraId, 
                "status", status, 
                "timestamp", System.currentTimeMillis()
            )
        );
    }

    /**
     * Publish camera health update
     */
    public void publishCameraHealthUpdate(Long cameraId, Map<String, Object> healthData) {
        log.debug("Publishing camera health update for camera: {}", cameraId);
        messagingTemplate.convertAndSend(
            "/topic/camera/" + cameraId + "/health", 
            healthData
        );
    }
}
