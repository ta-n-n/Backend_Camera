package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraHealthLog;
import com.example.camerasurveillancesystem.repository.CameraHealthLogRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.AiDetectionStreamService;
import com.example.camerasurveillancesystem.service.StreamMonitorService;
import com.example.camerasurveillancesystem.service.VideoStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamMonitorServiceImpl implements StreamMonitorService {

    private final VideoStreamService videoStreamService;
    private final AiDetectionStreamService aiDetectionStreamService;
    private final CameraRepository cameraRepository;
    private final CameraHealthLogRepository healthLogRepository;

    private final Map<Long, StreamHealthInfo> healthInfoMap = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> autoRestartEnabled = new ConcurrentHashMap<>();
    
    private volatile boolean monitoringEnabled = false;

    @PostConstruct
    public void init() {
        log.info("Initializing StreamMonitorService");
        // Không tự động start monitoring, để user control
    }

    @Override
    public void startMonitoring() {
        log.info("Starting stream monitoring");
        monitoringEnabled = true;
    }

    @Override
    public void stopMonitoring() {
        log.info("Stopping stream monitoring");
        monitoringEnabled = false;
    }

    /**
     * Scheduled task chạy mỗi 30 giây để kiểm tra health
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void monitorStreams() {
        if (!monitoringEnabled) {
            return;
        }

        log.debug("Running stream health check");

        try {
            // Lấy tất cả cameras đang active
            cameraRepository.findByStatus("ACTIVE").forEach(camera -> {
                Long cameraId = camera.getId();
                boolean isRunning = videoStreamService.isStreamRunning(cameraId);
                
                StreamHealthInfo healthInfo = healthInfoMap.computeIfAbsent(
                    cameraId, 
                    k -> new StreamHealthInfo()
                );

                if (isRunning) {
                    // Stream đang chạy
                    healthInfo.setHealthy(true);
                    healthInfo.setLastCheckTime(LocalDateTime.now());
                    healthInfo.resetFailureCount();
                    
                    // Log health status nếu trước đó bị unhealthy
                    if (healthInfo.wasUnhealthy()) {
                        logHealthStatus(camera, "HEALTHY", "Stream recovered");
                        healthInfo.setWasUnhealthy(false);
                    }
                } else {
                    // Stream không chạy
                    healthInfo.setHealthy(false);
                    healthInfo.setLastCheckTime(LocalDateTime.now());
                    healthInfo.incrementFailureCount();
                    healthInfo.setWasUnhealthy(true);

                    log.warn("Stream not running for camera {}, failure count: {}", 
                            cameraId, healthInfo.getFailureCount());

                    // Log health status
                    logHealthStatus(camera, "UNHEALTHY", 
                        "Stream not running, failure count: " + healthInfo.getFailureCount());

                    // Auto-restart nếu được enable
                    if (autoRestartEnabled.getOrDefault(cameraId, true)) {
                        if (healthInfo.getFailureCount() >= 3) {
                            log.info("Attempting auto-restart for camera {}", cameraId);
                            attemptAutoRestart(camera);
                        }
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error during stream monitoring: {}", e.getMessage(), e);
        }
    }

    private void attemptAutoRestart(Camera camera) {
        try {
            log.info("Auto-restarting stream for camera {}", camera.getId());
            
            // Dừng detection cũ nếu có
            aiDetectionStreamService.stopDetection(camera.getId());
            
            // Đợi một chút
            Thread.sleep(2000);
            
            // Khởi động lại detection
            boolean restarted = aiDetectionStreamService.startDetection(camera.getId());
            
            if (restarted) {
                log.info("Successfully auto-restarted stream for camera {}", camera.getId());
                logHealthStatus(camera, "RESTARTED", "Auto-restart successful");
                
                // Reset failure count
                StreamHealthInfo healthInfo = healthInfoMap.get(camera.getId());
                if (healthInfo != null) {
                    healthInfo.resetFailureCount();
                }
            } else {
                log.error("Failed to auto-restart stream for camera {}", camera.getId());
                logHealthStatus(camera, "RESTART_FAILED", "Auto-restart failed");
            }
            
        } catch (Exception e) {
            log.error("Error auto-restarting stream for camera {}: {}", 
                    camera.getId(), e.getMessage(), e);
            logHealthStatus(camera, "RESTART_ERROR", "Error: " + e.getMessage());
        }
    }

    @Transactional
    protected void logHealthStatus(Camera camera, String status, String message) {
        try {
            CameraHealthLog healthLog = new CameraHealthLog();
            healthLog.setCamera(camera);
            healthLog.setStatus(status);
            healthLog.setMessage(message);
            healthLog.setCheckedAt(LocalDateTime.now());
            
            healthLogRepository.save(healthLog);
            
            log.debug("Logged health status for camera {}: {} - {}", 
                    camera.getId(), status, message);
                    
        } catch (Exception e) {
            log.error("Error logging health status for camera {}: {}", 
                    camera.getId(), e.getMessage(), e);
        }
    }

    @Override
    public boolean isStreamHealthy(Long cameraId) {
        StreamHealthInfo healthInfo = healthInfoMap.get(cameraId);
        return healthInfo != null && healthInfo.isHealthy();
    }

    @Override
    public Map<Long, Boolean> getAllStreamHealthStatus() {
        Map<Long, Boolean> statusMap = new HashMap<>();
        healthInfoMap.forEach((cameraId, healthInfo) -> 
            statusMap.put(cameraId, healthInfo.isHealthy())
        );
        return statusMap;
    }

    @Override
    public void setAutoRestart(Long cameraId, boolean enabled) {
        autoRestartEnabled.put(cameraId, enabled);
        log.info("Auto-restart {} for camera {}", enabled ? "enabled" : "disabled", cameraId);
    }

    @Override
    @Transactional
    public boolean manualRestartStream(Long cameraId) {
        log.info("Manual restart requested for camera {}", cameraId);
        
        Camera camera = cameraRepository.findById(cameraId).orElse(null);
        if (camera == null) {
            log.error("Camera {} not found", cameraId);
            return false;
        }
        
        attemptAutoRestart(camera);
        
        // Kiểm tra xem restart có thành công không
        try {
            Thread.sleep(3000); // Đợi stream khởi động
            return videoStreamService.isStreamRunning(cameraId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Cleanup old health logs mỗi 1 giờ
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOldHealthLogs() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            healthLogRepository.deleteByCheckedAtBefore(cutoffDate);
            log.info("Cleaned up old health logs before {}", cutoffDate);
        } catch (Exception e) {
            log.error("Error cleaning up old health logs: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up StreamMonitorService");
        stopMonitoring();
    }

    /**
     * Class để lưu thông tin health của stream
     */
    private static class StreamHealthInfo {
        private volatile boolean healthy;
        private volatile LocalDateTime lastCheckTime;
        private volatile int failureCount;
        private volatile boolean wasUnhealthy;

        public StreamHealthInfo() {
            this.healthy = false;
            this.lastCheckTime = LocalDateTime.now();
            this.failureCount = 0;
            this.wasUnhealthy = false;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public LocalDateTime getLastCheckTime() {
            return lastCheckTime;
        }

        public void setLastCheckTime(LocalDateTime lastCheckTime) {
            this.lastCheckTime = lastCheckTime;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void incrementFailureCount() {
            this.failureCount++;
        }

        public void resetFailureCount() {
            this.failureCount = 0;
        }

        public boolean wasUnhealthy() {
            return wasUnhealthy;
        }

        public void setWasUnhealthy(boolean wasUnhealthy) {
            this.wasUnhealthy = wasUnhealthy;
        }
    }
}
