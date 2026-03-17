package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.*;
import com.example.camerasurveillancesystem.repository.*;
import com.example.camerasurveillancesystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AlertRepository alertRepository;
    private final AiEventRepository aiEventRepository;
    private final SystemLogRepository systemLogRepository;
    private final VideoRecordRepository videoRecordRepository;
    private final SnapshotImageRepository snapshotImageRepository;
    private final CameraRepository cameraRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ByteArrayOutputStream generateAlertReportPdf(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating PDF report for alerts from {} to {}", startDate, endDate);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // TODO: Integrate with PDF library (iText, Apache PDFBox, etc.)
            // For now, generate a simple text-based report
            
            List<Alert> alerts = alertRepository.findAll(); // Should filter by date
            
            StringBuilder content = new StringBuilder();
            content.append("ALERT REPORT\n");
            content.append("Period: ").append(startDate.format(DATE_FORMATTER))
                   .append(" to ").append(endDate.format(DATE_FORMATTER)).append("\n\n");
            
            content.append(String.format("Total Alerts: %d\n\n", alerts.size()));
            
            for (Alert alert : alerts) {
                content.append(String.format("Alert ID: %d\n", alert.getId()));
                content.append(String.format("Title: %s\n", alert.getTitle()));
                content.append(String.format("Severity: %s\n", alert.getSeverity()));
                content.append(String.format("Status: %s\n", alert.getStatus()));
                String cameraName = "N/A";
                if (alert.getAiEvent() != null && alert.getAiEvent().getCamera() != null) {
                    cameraName = alert.getAiEvent().getCamera().getName();
                }
                content.append(String.format("Camera: %s\n", cameraName));
                content.append(String.format("Created: %s\n", alert.getCreatedAt().format(DATE_FORMATTER)));
                content.append("---\n");
            }
            
            baos.write(content.toString().getBytes());
            
            log.info("PDF report generated successfully");
        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF report", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream generateAiDetectionReportPdf(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating PDF report for AI detections from {} to {}", startDate, endDate);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            List<AiEvent> events = aiEventRepository.findAll(); // Should filter by date
            
            StringBuilder content = new StringBuilder();
            content.append("AI DETECTION REPORT\n");
            content.append("Period: ").append(startDate.format(DATE_FORMATTER))
                   .append(" to ").append(endDate.format(DATE_FORMATTER)).append("\n\n");
            
            content.append(String.format("Total Detections: %d\n\n", events.size()));
            
            for (AiEvent event : events) {
                content.append(String.format("Event ID: %d\n", event.getId()));
                content.append(String.format("Type: %s\n", event.getEventType()));
                content.append(String.format("Confidence: %.2f%%\n", event.getConfidenceScore()));
                content.append(String.format("Detected: %s\n", event.getDetectedAt().format(DATE_FORMATTER)));
                content.append("---\n");
            }
            
            baos.write(content.toString().getBytes());
            
            log.info("AI detection PDF report generated successfully");
        } catch (Exception e) {
            log.error("Error generating AI detection PDF report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF report", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream generateCameraActivityReportPdf(Long cameraId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating PDF report for camera {} from {} to {}", cameraId, startDate, endDate);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            Camera camera = cameraRepository.findById(cameraId)
                .orElseThrow(() -> new RuntimeException("Camera not found"));
            
            StringBuilder content = new StringBuilder();
            content.append("CAMERA ACTIVITY REPORT\n");
            content.append(String.format("Camera: %s\n", camera.getName()));
            content.append("Period: ").append(startDate.format(DATE_FORMATTER))
                   .append(" to ").append(endDate.format(DATE_FORMATTER)).append("\n\n");
            
            // Add camera statistics
            content.append("Status: ").append(camera.getStatus()).append("\n");
            content.append("Location: ").append(camera.getLocation() != null ? camera.getLocation().getName() : "N/A").append("\n\n");
            
            baos.write(content.toString().getBytes());
            
            log.info("Camera activity PDF report generated successfully");
        } catch (Exception e) {
            log.error("Error generating camera activity PDF report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF report", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream generateAlertReportExcel(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating Excel report for alerts from {} to {}", startDate, endDate);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // TODO: Integrate with Apache POI for Excel generation
            // For now, generate CSV format
            
            List<Alert> alerts = alertRepository.findAll(); // Should filter by date
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Title,Severity,Status,Camera,Created At\n");
            
            for (Alert alert : alerts) {
                String cameraName = "N/A";
                if (alert.getAiEvent() != null && alert.getAiEvent().getCamera() != null) {
                    cameraName = alert.getAiEvent().getCamera().getName();
                }
                csv.append(String.format("%d,\"%s\",%s,%s,%s,%s\n",
                    alert.getId(),
                    alert.getTitle(),
                    alert.getSeverity(),
                    alert.getStatus(),
                    cameraName,
                    alert.getCreatedAt().format(DATE_FORMATTER)
                ));
            }
            
            baos.write(csv.toString().getBytes());
            
            log.info("Excel report generated successfully");
        } catch (Exception e) {
            log.error("Error generating Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream generateAiDetectionReportExcel(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating Excel report for AI detections from {} to {}", startDate, endDate);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            List<AiEvent> events = aiEventRepository.findAll(); // Should filter by date
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Event Type,Confidence,Camera,Detected At\n");
            
            for (AiEvent event : events) {
                csv.append(String.format("%d,\"%s\",%.2f,%s,%s\n",
                    event.getId(),
                    event.getEventType(),
                    event.getConfidenceScore(),
                    event.getCamera() != null ? event.getCamera().getName() : "N/A",
                    event.getDetectedAt().format(DATE_FORMATTER)
                ));
            }
            
            baos.write(csv.toString().getBytes());
            
            log.info("AI detection Excel report generated successfully");
        } catch (Exception e) {
            log.error("Error generating AI detection Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream generateSystemLogReportExcel(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating Excel report for system logs from {} to {}", startDate, endDate);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            List<SystemLog> logs = systemLogRepository.findByCreatedAtBetween(startDate, endDate, org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Level,Module,Message,Created At\n");
            
            for (SystemLog log : logs) {
                csv.append(String.format("%d,%s,%s,\"%s\",%s\n",
                    log.getId(),
                    log.getLevel(),
                    log.getModule(),
                    log.getMessage(),
                    log.getCreatedAt().format(DATE_FORMATTER)
                ));
            }
            
            baos.write(csv.toString().getBytes());
            
            log.info("System log Excel report generated successfully");
        } catch (Exception e) {
            log.error("Error generating system log Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream exportVideosAsZip(List<Long> videoIds) {
        log.info("Exporting {} videos as ZIP", videoIds.size());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Long videoId : videoIds) {
                VideoRecord video = videoRecordRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found: " + videoId));
                
                String filename = String.format("video_%d.mp4", video.getId());
                ZipEntry zipEntry = new ZipEntry(filename);
                zos.putNextEntry(zipEntry);
                
                // TODO: Read actual video file and write to ZIP
                // For now, write placeholder
                zos.write(("Video content for ID: " + videoId).getBytes());
                
                zos.closeEntry();
            }
            
            log.info("Videos exported successfully as ZIP");
        } catch (Exception e) {
            log.error("Error exporting videos as ZIP: {}", e.getMessage());
            throw new RuntimeException("Failed to export videos", e);
        }
        
        return baos;
    }

    @Override
    public ByteArrayOutputStream exportSnapshotsAsZip(List<Long> snapshotIds) {
        log.info("Exporting {} snapshots as ZIP", snapshotIds.size());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Long snapshotId : snapshotIds) {
                SnapshotImage snapshot = snapshotImageRepository.findById(snapshotId)
                    .orElseThrow(() -> new RuntimeException("Snapshot not found: " + snapshotId));
                
                String filename = String.format("snapshot_%d.jpg", snapshot.getId());
                ZipEntry zipEntry = new ZipEntry(filename);
                zos.putNextEntry(zipEntry);
                
                // TODO: Read actual snapshot file and write to ZIP
                // For now, write placeholder
                zos.write(("Snapshot content for ID: " + snapshotId).getBytes());
                
                zos.closeEntry();
            }
            
            log.info("Snapshots exported successfully as ZIP");
        } catch (Exception e) {
            log.error("Error exporting snapshots as ZIP: {}", e.getMessage());
            throw new RuntimeException("Failed to export snapshots", e);
        }
        
        return baos;
    }
}
