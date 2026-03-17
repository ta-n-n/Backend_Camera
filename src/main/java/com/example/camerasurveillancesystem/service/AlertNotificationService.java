package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.alert.AlertNotificationCreateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertNotificationResponse;

import java.util.List;

public interface AlertNotificationService {

    /**
     * Tạo và gửi notification
     */
    AlertNotificationResponse sendNotification(AlertNotificationCreateRequest request);

    /**
     * Retry failed notification
     */
    AlertNotificationResponse retryNotification(Long id);

    /**
     * Get notifications by alert
     */
    List<AlertNotificationResponse> getNotificationsByAlertId(Long alertId);

    /**
     * Get notifications by channel
     */
    PageResponse<AlertNotificationResponse> getNotificationsByChannelId(Long channelId, Integer page, Integer size);

    /**
     * Get failed notifications
     */
    List<AlertNotificationResponse> getFailedNotifications(Integer limit);

    /**
     * Retry all failed notifications
     */
    void retryAllFailedNotifications();
}
