package com.example.camerasurveillancesystem.config;

import com.example.camerasurveillancesystem.service.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final VideoRecordServiceImpl videoRecordService;
    private final SnapshotImageServiceImpl snapshotImageService;
    private final SystemLogServiceImpl systemLogService;
    private final CameraServiceImpl cameraService;

    /**
     * Cleanup old videos daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldVideos() {
        log.info("Starting scheduled cleanup of old videos");
        try {
            videoRecordService.cleanupOldVideos(30); // Keep 30 days
            log.info("Completed scheduled cleanup of old videos");
        } catch (Exception e) {
            log.error("Error during scheduled video cleanup: {}", e.getMessage());
        }
    }

    /**
     * Cleanup old snapshots daily at 2:30 AM
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupOldSnapshots() {
        log.info("Starting scheduled cleanup of old snapshots");
        try {
            snapshotImageService.cleanupOldSnapshots(15); // Keep 15 days
            log.info("Completed scheduled cleanup of old snapshots");
        } catch (Exception e) {
            log.error("Error during scheduled snapshot cleanup: {}", e.getMessage());
        }
    }

    /**
     * Cleanup old system logs weekly on Sunday at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupOldLogs() {
        log.info("Starting scheduled cleanup of old system logs");
        try {
            systemLogService.deleteOldLogs(90); // Keep 90 days
            log.info("Completed scheduled cleanup of old system logs");
        } catch (Exception e) {
            log.error("Error during scheduled log cleanup: {}", e.getMessage());
        }
    }

    /**
     * Check camera health every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkCameraHealth() {
        log.debug("Starting scheduled camera health check");
        try {
            // TODO: Implement camera health check logic
            // This would ping cameras, check streams, update status
            log.debug("Completed scheduled camera health check");
        } catch (Exception e) {
            log.error("Error during scheduled camera health check: {}", e.getMessage());
        }
    }

    /**
     * Archive old recordings monthly on 1st day at 4:00 AM
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void archiveOldRecordings() {
        log.info("Starting scheduled archiving of old recordings");
        try {
            // TODO: Implement archiving logic
            // This would move old recordings to archive storage
            log.info("Completed scheduled archiving of old recordings");
        } catch (Exception e) {
            log.error("Error during scheduled archiving: {}", e.getMessage());
        }
    }

    /**
     * Generate daily reports at 6:00 AM
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void generateDailyReports() {
        log.info("Starting scheduled daily report generation");
        try {
            // TODO: Implement daily report generation
            log.info("Completed scheduled daily report generation");
        } catch (Exception e) {
            log.error("Error during scheduled report generation: {}", e.getMessage());
        }
    }
}
