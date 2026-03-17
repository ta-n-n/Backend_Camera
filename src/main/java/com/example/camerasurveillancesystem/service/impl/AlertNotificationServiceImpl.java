package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Alert;
import com.example.camerasurveillancesystem.domain.AlertNotification;
import com.example.camerasurveillancesystem.domain.NotificationChannel;
import com.example.camerasurveillancesystem.dto.mapper.AlertNotificationMapper;
import com.example.camerasurveillancesystem.dto.request.alert.AlertNotificationCreateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertNotificationResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.AlertNotificationRepository;
import com.example.camerasurveillancesystem.repository.AlertRepository;
import com.example.camerasurveillancesystem.repository.NotificationChannelRepository;
import com.example.camerasurveillancesystem.service.AlertNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private final AlertNotificationRepository notificationRepository;
    private final AlertRepository alertRepository;
    private final NotificationChannelRepository channelRepository;
    private final AlertNotificationMapper notificationMapper;

    @Override
    @Transactional
    public AlertNotificationResponse sendNotification(AlertNotificationCreateRequest request) {
        AlertNotification notification = new AlertNotification();
        
        // Set alert
        Alert alert = alertRepository.findById(request.getAlertId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        notification.setAlert(alert);
        
        // Set channel
        NotificationChannel channel = channelRepository.findById(request.getNotificationChannelId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND));
        notification.setChannel(channel);
        
        // Set recipient and message
        notification.setRecipientAddress(request.getRecipient());
        notification.setMessage(request.getMessage());
        notification.setStatus("PENDING");
        notification.setRetryCount(0);
        
        AlertNotification savedNotification = notificationRepository.save(notification);
        
        // Try to send immediately
        try {
            savedNotification.setStatus("SENT");
            savedNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(savedNotification);
            log.info("Sent notification {} for alert {}", savedNotification.getId(), alert.getId());
        } catch (Exception e) {
            savedNotification.setStatus("FAILED");
            savedNotification.setErrorMessage(e.getMessage());
            notificationRepository.save(savedNotification);
            log.error("Failed to send notification: {}", e.getMessage());
        }
        
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional
    public AlertNotificationResponse retryNotification(Long id) {
        AlertNotification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        
        if (!"FAILED".equals(notification.getStatus())) {
            throw new IllegalStateException("Can only retry failed notifications");
        }
        
        try {
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);
            notification.setRetryCount(notification.getRetryCount() + 1);
            
            AlertNotification updated = notificationRepository.save(notification);
            log.info("Retried notification {}", id);
            
            return notificationMapper.toResponse(updated);
        } catch (Exception e) {
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
            
            log.error("Failed to retry notification {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retry notification", e);
        }
    }

    @Override
    public List<AlertNotificationResponse> getNotificationsByAlertId(Long alertId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "sentAt"));
        return notificationRepository.findByAlertIdOrderBySentAtDesc(alertId, pageable)
            .map(notificationMapper::toResponse)
            .getContent();
    }

    @Override
    public PageResponse<AlertNotificationResponse> getNotificationsByChannelId(Long channelId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        var notificationPage = notificationRepository.findByChannelIdOrderBySentAtDesc(channelId, pageable);
        
        return PageResponse.<AlertNotificationResponse>builder()
            .content(notificationPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(notificationPage.getNumber())
            .pageSize(notificationPage.getSize())
            .totalElements(notificationPage.getTotalElements())
            .totalPages(notificationPage.getTotalPages())
            .last(notificationPage.isLast())
            .build();
    }

    @Override
    public List<AlertNotificationResponse> getFailedNotifications(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sentAt"));
        return notificationRepository.findByStatusOrderBySentAtDesc("FAILED", pageable)
            .map(notificationMapper::toResponse)
            .getContent();
    }

    @Override
    @Transactional
    public void retryAllFailedNotifications() {
        List<AlertNotification> failedNotifications = notificationRepository
            .findFailedNotificationsForRetry(3);
        
        log.info("Retrying {} failed notifications", failedNotifications.size());
        
        for (AlertNotification notification : failedNotifications) {
            try {
                retryNotification(notification.getId());
            } catch (Exception e) {
                log.error("Failed to retry notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }
}
