package com.example.camerasurveillancesystem.service;

import java.util.Map;

/**
 * Service để monitor và quản lý stream health
 */
public interface StreamMonitorService {

    /**
     * Bắt đầu monitoring tất cả streams
     */
    void startMonitoring();

    /**
     * Dừng monitoring
     */
    void stopMonitoring();

    /**
     * Kiểm tra health của stream
     * 
     * @param cameraId ID của camera
     * @return true nếu stream healthy
     */
    boolean isStreamHealthy(Long cameraId);

    /**
     * Lấy thông tin health của tất cả streams
     * 
     * @return Map<cameraId, healthy>
     */
    Map<Long, Boolean> getAllStreamHealthStatus();

    /**
     * Enable/disable auto-restart cho stream
     * 
     * @param cameraId ID của camera
     * @param enabled true để enable auto-restart
     */
    void setAutoRestart(Long cameraId, boolean enabled);

    /**
     * Restart stream thủ công
     * 
     * @param cameraId ID của camera
     * @return true nếu restart thành công
     */
    boolean manualRestartStream(Long cameraId);
}
