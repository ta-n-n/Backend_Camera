package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.repository.*;
import com.example.camerasurveillancesystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CameraRepository cameraRepository;
    private final AlertRepository alertRepository;
    private final AiEventRepository aiEventRepository;
    private final VideoRecordRepository videoRecordRepository;
    private final SnapshotImageRepository snapshotImageRepository;
    private final SystemLogRepository systemLogRepository;

    @Override
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCameras", cameraRepository.count());
        stats.put("activeCameras", cameraRepository.countByStatus("ONLINE"));
        stats.put("totalAlerts", alertRepository.count());
        stats.put("totalAiEvents", aiEventRepository.count());
        stats.put("totalVideos", videoRecordRepository.count());
        stats.put("totalSnapshots", snapshotImageRepository.count());
        
        return stats;
    }

    @Override
    public Map<String, Object> getCameraStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCameras = cameraRepository.count();
        long onlineCameras = cameraRepository.countByStatus("ONLINE");
        long offlineCameras = cameraRepository.countByStatus("OFFLINE");
        
        stats.put("total", totalCameras);
        stats.put("online", onlineCameras);
        stats.put("offline", offlineCameras);
        stats.put("uptimePercentage", totalCameras > 0 ? (double) onlineCameras / totalCameras * 100 : 0);
        
        return stats;
    }

    @Override
    public Map<String, Object> getAlertStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalAlerts = alertRepository.count();
        long criticalAlerts = alertRepository.countBySeverity("CRITICAL");
        long highAlerts = alertRepository.countBySeverity("HIGH");
        long resolvedAlerts = alertRepository.countByStatus("RESOLVED");
        
        stats.put("total", totalAlerts);
        stats.put("critical", criticalAlerts);
        stats.put("high", highAlerts);
        stats.put("resolved", resolvedAlerts);
        stats.put("resolutionRate", totalAlerts > 0 ? (double) resolvedAlerts / totalAlerts * 100 : 0);
        
        return stats;
    }

    @Override
    public Map<String, Object> getAiDetectionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalEvents = aiEventRepository.count();
        
        stats.put("total", totalEvents);
        stats.put("avgConfidence", aiEventRepository.findAll().stream()
            .mapToDouble(event -> event.getConfidenceScore() != null ? event.getConfidenceScore() : 0.0)
            .average()
            .orElse(0.0));
        
        return stats;
    }

    @Override
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalVideos = videoRecordRepository.count();
        long totalSnapshots = snapshotImageRepository.count();
        
        // Calculate total size
        long totalVideoSize = videoRecordRepository.findAll().stream()
            .mapToLong(video -> video.getFileSize() != null ? video.getFileSize() : 0)
            .sum();
        
        long totalSnapshotSize = snapshotImageRepository.findAll().stream()
            .mapToLong(snapshot -> snapshot.getFileSize() != null ? snapshot.getFileSize() : 0)
            .sum();
        
        stats.put("totalVideos", totalVideos);
        stats.put("totalSnapshots", totalSnapshots);
        stats.put("totalVideoSize", totalVideoSize);
        stats.put("totalSnapshotSize", totalSnapshotSize);
        stats.put("totalSize", totalVideoSize + totalSnapshotSize);
        
        return stats;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get error rate
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long totalLogs = systemLogRepository.countByLevelAndCreatedAtAfter("INFO", last24Hours) +
                        systemLogRepository.countByLevelAndCreatedAtAfter("ERROR", last24Hours);
        long errorLogs = systemLogRepository.countByLevelAndCreatedAtAfter("ERROR", last24Hours);
        
        metrics.put("errorRate", totalLogs > 0 ? (double) errorLogs / totalLogs * 100 : 0);
        metrics.put("totalLogs24h", totalLogs);
        metrics.put("errors24h", errorLogs);
        
        return metrics;
    }

    @Override
    public Map<String, Object> getActivityTimeline(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> timeline = new HashMap<>();
        
        // Get activity counts by date
        // This is a simplified version - in production, you'd want more sophisticated grouping
        timeline.put("alerts", alertRepository.count());
        timeline.put("aiEvents", aiEventRepository.count());
        timeline.put("recordings", videoRecordRepository.count());
        
        return timeline;
    }
}
