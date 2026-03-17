package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.alert.AlertNotificationCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertNotificationResponse;
import com.example.camerasurveillancesystem.service.AlertNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alert-notifications")
@RequiredArgsConstructor
public class AlertNotificationController {

    private final AlertNotificationService notificationService;

    /**
     * Tạo và gửi notification
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AlertNotificationResponse>> sendNotification(
            @Valid @RequestBody AlertNotificationCreateRequest request) {
        AlertNotificationResponse notification = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification sent successfully", notification));
    }

    /**
     * Retry failed notification
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<AlertNotificationResponse>> retryNotification(
            @PathVariable Long id) {
        AlertNotificationResponse notification = notificationService.retryNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification retry initiated", notification));
    }

    /**
     * Retry tất cả failed notifications
     */
    @PostMapping("/retry-all-failed")
    public ResponseEntity<ApiResponse<Void>> retryAllFailedNotifications() {
        notificationService.retryAllFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success("Retrying all failed notifications", null));
    }

    /**
     * Get notifications theo alert
     */
    @GetMapping("/alert/{alertId}")
    public ResponseEntity<ApiResponse<List<AlertNotificationResponse>>> getNotificationsByAlert(
            @PathVariable Long alertId) {
        List<AlertNotificationResponse> notifications = 
                notificationService.getNotificationsByAlertId(alertId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get notifications theo channel
     */
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ApiResponse<PageResponse<AlertNotificationResponse>>> getNotificationsByChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<AlertNotificationResponse> notifications = 
                notificationService.getNotificationsByChannelId(channelId, page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get failed notifications
     */
    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<AlertNotificationResponse>>> getFailedNotifications(
            @RequestParam(defaultValue = "50") int limit) {
        List<AlertNotificationResponse> notifications = 
                notificationService.getFailedNotifications(limit);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
}
