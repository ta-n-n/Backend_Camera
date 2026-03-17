package com.example.camerasurveillancesystem.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    /**
     * Generate PDF report for alerts
     */
    ByteArrayOutputStream generateAlertReportPdf(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate PDF report for AI detections
     */
    ByteArrayOutputStream generateAiDetectionReportPdf(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate PDF report for camera activity
     */
    ByteArrayOutputStream generateCameraActivityReportPdf(Long cameraId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate Excel report for alerts
     */
    ByteArrayOutputStream generateAlertReportExcel(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate Excel report for AI detections
     */
    ByteArrayOutputStream generateAiDetectionReportExcel(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate Excel report for system logs
     */
    ByteArrayOutputStream generateSystemLogReportExcel(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Export videos as ZIP archive
     */
    ByteArrayOutputStream exportVideosAsZip(List<Long> videoIds);

    /**
     * Export snapshots as ZIP archive
     */
    ByteArrayOutputStream exportSnapshotsAsZip(List<Long> snapshotIds);
}
