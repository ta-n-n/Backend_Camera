package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generate alert report in PDF format
     */
    @GetMapping("/alerts/pdf")
    public ResponseEntity<byte[]> generateAlertReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ByteArrayOutputStream report = reportService.generateAlertReportPdf(startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "alert_report.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.toByteArray());
    }

    /**
     * Generate AI detection report in PDF format
     */
    @GetMapping("/ai-detections/pdf")
    public ResponseEntity<byte[]> generateAiDetectionReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ByteArrayOutputStream report = reportService.generateAiDetectionReportPdf(startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ai_detection_report.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.toByteArray());
    }

    /**
     * Generate camera activity report in PDF format
     */
    @GetMapping("/cameras/{cameraId}/pdf")
    public ResponseEntity<byte[]> generateCameraActivityReportPdf(
            @PathVariable Long cameraId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ByteArrayOutputStream report = reportService.generateCameraActivityReportPdf(cameraId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", String.format("camera_%d_report.pdf", cameraId));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.toByteArray());
    }

    /**
     * Generate alert report in Excel format
     */
    @GetMapping("/alerts/excel")
    public ResponseEntity<byte[]> generateAlertReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ByteArrayOutputStream report = reportService.generateAlertReportExcel(startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "alert_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.toByteArray());
    }

    /**
     * Generate AI detection report in Excel format
     */
    @GetMapping("/ai-detections/excel")
    public ResponseEntity<byte[]> generateAiDetectionReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ByteArrayOutputStream report = reportService.generateAiDetectionReportExcel(startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "ai_detection_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.toByteArray());
    }

    /**
     * Generate system log report in Excel format
     */
    @GetMapping("/system-logs/excel")
    public ResponseEntity<byte[]> generateSystemLogReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ByteArrayOutputStream report = reportService.generateSystemLogReportExcel(startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "system_log_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.toByteArray());
    }

    /**
     * Export videos as ZIP archive
     */
    @PostMapping("/videos/export")
    public ResponseEntity<byte[]> exportVideos(@RequestBody List<Long> videoIds) {
        ByteArrayOutputStream zipFile = reportService.exportVideosAsZip(videoIds);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", "videos.zip");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(zipFile.toByteArray());
    }

    /**
     * Export snapshots as ZIP archive
     */
    @PostMapping("/snapshots/export")
    public ResponseEntity<byte[]> exportSnapshots(@RequestBody List<Long> snapshotIds) {
        ByteArrayOutputStream zipFile = reportService.exportSnapshotsAsZip(snapshotIds);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", "snapshots.zip");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(zipFile.toByteArray());
    }
}
