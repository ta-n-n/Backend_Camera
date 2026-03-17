package com.example.camerasurveillancesystem.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface DashboardService {

    /**
     * Get overall system statistics
     */
    Map<String, Object> getSystemStatistics();

    /**
     * Get camera statistics
     */
    Map<String, Object> getCameraStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get alert statistics
     */
    Map<String, Object> getAlertStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get AI detection statistics
     */
    Map<String, Object> getAiDetectionStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get storage statistics
     */
    Map<String, Object> getStorageStatistics();

    /**
     * Get performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Get activity timeline
     */
    Map<String, Object> getActivityTimeline(LocalDateTime startDate, LocalDateTime endDate);
}
