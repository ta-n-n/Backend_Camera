package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.ai.detector.DetectionResult;
import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.dto.request.StreamDetectionConfigRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.DetectionStreamResponse;
import com.example.camerasurveillancesystem.dto.request.StreamAutoAlertConfigRequest;
import com.example.camerasurveillancesystem.dto.response.StreamAutoAlertConfigResponse;
import com.example.camerasurveillancesystem.dto.response.StreamStatusResponse;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.AiDetectionStreamService;
import com.example.camerasurveillancesystem.service.StreamMonitorService;
import com.example.camerasurveillancesystem.service.VideoStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
@Tag(name = "Stream Processing", description = "APIs for video stream processing and AI detection")
public class StreamProcessingController {

    private final AiDetectionStreamService aiDetectionStreamService;
    private final VideoStreamService videoStreamService;
    private final StreamMonitorService streamMonitorService;
    private final CameraRepository cameraRepository;

    @PostMapping("/detection/start/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Start AI detection for a camera")
    public ResponseEntity<ApiResponse<StreamStatusResponse>> startDetection(@PathVariable Long cameraId) {
        log.info("Starting detection for camera {}", cameraId);

        Camera camera = cameraRepository.findById(cameraId).orElse(null);
        if (camera == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<StreamStatusResponse>builder()
                    .success(false)
                    .message("Camera không tồn tại")
                    .build());
        }

        boolean started = aiDetectionStreamService.startDetection(cameraId);

        if (started) {
            StreamStatusResponse response = StreamStatusResponse.builder()
                .cameraId(cameraId)
                .cameraName(camera.getName())
                .rtspUrl(camera.getRtspUrl())
                .isRunning(true)
                .status("RUNNING")
                .message("Detection started successfully")
                .lastCheckTime(LocalDateTime.now())
                .build();

            return ResponseEntity.ok(ApiResponse.<StreamStatusResponse>builder()
                .success(true)
                .message("Đã bắt đầu AI detection cho camera " + camera.getName())
                .data(response)
                .build());
        } else {
            String errorDetail = aiDetectionStreamService.getLastStartError(cameraId);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<StreamStatusResponse>builder()
                    .success(false)
                    .message(errorDetail != null
                            ? errorDetail
                            : "Không thể bắt đầu detection cho camera " + camera.getName())
                    .build());
        }
    }

    @PostMapping("/detection/stop/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Stop AI detection for a camera")
    public ResponseEntity<ApiResponse<String>> stopDetection(@PathVariable Long cameraId) {
        log.info("Stopping detection for camera {}", cameraId);

        aiDetectionStreamService.stopDetection(cameraId);

        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Đã dừng AI detection cho camera " + cameraId)
            .build());
    }

    @PostMapping("/detection/start-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Start AI detection for all active cameras")
    public ResponseEntity<ApiResponse<String>> startDetectionForAll() {
        log.info("Starting detection for all active cameras");

        int count = aiDetectionStreamService.startDetectionForAllActiveCameras();

        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Đã bắt đầu AI detection cho " + count + " cameras")
            .data(String.valueOf(count))
            .build());
    }

    @PostMapping("/detection/stop-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Stop AI detection for all cameras")
    public ResponseEntity<ApiResponse<String>> stopDetectionForAll() {
        log.info("Stopping detection for all cameras");

        aiDetectionStreamService.stopAllDetections();

        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Đã dừng tất cả AI detection")
            .build());
    }

    @PostMapping("/detection/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Configure detection parameters")
    public ResponseEntity<ApiResponse<String>> configureDetection(
            @Valid @RequestBody StreamDetectionConfigRequest request) {
        log.info("Configuring detection for camera {}", request.getCameraId());

        if (request.getConfidenceThreshold() != null) {
            aiDetectionStreamService.setConfidenceThreshold(
                request.getCameraId(), 
                request.getConfidenceThreshold()
            );
        }

        if (request.getFrameSkip() != null) {
            aiDetectionStreamService.setFrameSkip(
                request.getCameraId(), 
                request.getFrameSkip()
            );
        }

        if (request.getAutoRestart() != null) {
            streamMonitorService.setAutoRestart(
                request.getCameraId(), 
                request.getAutoRestart()
            );
        }

        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Đã cập nhật cấu hình detection cho camera " + request.getCameraId())
            .build());
    }

    @PostMapping("/detection/auto-alert/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Configure automatic alert creation from AI detections")
    public ResponseEntity<ApiResponse<StreamAutoAlertConfigResponse>> configureAutoAlert(
            @Valid @RequestBody StreamAutoAlertConfigRequest request) {
        log.info("Configuring auto-alert for camera {}", request.getCameraId());

        aiDetectionStreamService.configureAutoAlert(request);
        StreamAutoAlertConfigResponse response = aiDetectionStreamService.getAutoAlertConfig(request.getCameraId());

        return ResponseEntity.ok(ApiResponse.<StreamAutoAlertConfigResponse>builder()
                .success(true)
                .message("Đã cập nhật cấu hình auto-alert cho camera " + request.getCameraId())
                .data(response)
                .build());
    }

    @GetMapping("/detection/auto-alert/config/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get auto-alert configuration for a camera")
    public ResponseEntity<ApiResponse<StreamAutoAlertConfigResponse>> getAutoAlertConfig(@PathVariable Long cameraId) {
        StreamAutoAlertConfigResponse response = aiDetectionStreamService.getAutoAlertConfig(cameraId);

        return ResponseEntity.ok(ApiResponse.<StreamAutoAlertConfigResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/status/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get stream status for a camera")
    public ResponseEntity<ApiResponse<StreamStatusResponse>> getStreamStatus(@PathVariable Long cameraId) {
        try {
            Camera camera = cameraRepository.findById(cameraId).orElse(null);
            if (camera == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<StreamStatusResponse>builder()
                        .success(false)
                        .message("Camera không tồn tại")
                        .build());
            }

            boolean isRunning = videoStreamService.isStreamRunning(cameraId);
            
            // Handle case when StreamMonitorService might throw exception
            boolean isHealthy = false;
            try {
                isHealthy = streamMonitorService.isStreamHealthy(cameraId);
            } catch (Exception e) {
                log.warn("Could not get health status for camera {}: {}", cameraId, e.getMessage());
                // Fallback: if stream is running, consider it healthy
                isHealthy = isRunning;
            }

            StreamStatusResponse response = StreamStatusResponse.builder()
                .cameraId(cameraId)
                .cameraName(camera.getName())
                .rtspUrl(camera.getRtspUrl())
                .isRunning(isRunning)
                .isHealthy(isHealthy)
                .status(isRunning ? "RUNNING" : "STOPPED")
                .lastCheckTime(LocalDateTime.now())
                .build();

            return ResponseEntity.ok(ApiResponse.<StreamStatusResponse>builder()
                .success(true)
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error getting stream status for camera {}: {}", cameraId, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.<StreamStatusResponse>builder()
                    .success(false)
                    .message("Lỗi server: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/status/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get status for all streams")
    public ResponseEntity<ApiResponse<List<StreamStatusResponse>>> getAllStreamStatus() {
        List<Camera> cameras = cameraRepository.findByStatus("ACTIVE");
        List<StreamStatusResponse> statusList = new ArrayList<>();

        for (Camera camera : cameras) {
            boolean isRunning = videoStreamService.isStreamRunning(camera.getId());
            boolean isHealthy = streamMonitorService.isStreamHealthy(camera.getId());

            StreamStatusResponse response = StreamStatusResponse.builder()
                .cameraId(camera.getId())
                .cameraName(camera.getName())
                .rtspUrl(camera.getRtspUrl())
                .isRunning(isRunning)
                .isHealthy(isHealthy)
                .status(isRunning ? "RUNNING" : "STOPPED")
                .lastCheckTime(LocalDateTime.now())
                .build();

            statusList.add(response);
        }

        return ResponseEntity.ok(ApiResponse.<List<StreamStatusResponse>>builder()
            .success(true)
            .data(statusList)
            .build());
    }

    @GetMapping("/detection/latest/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get latest detections for a camera")
    public ResponseEntity<ApiResponse<DetectionStreamResponse>> getLatestDetections(@PathVariable Long cameraId) {
        Camera camera = cameraRepository.findById(cameraId).orElse(null);
        if (camera == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<DetectionStreamResponse>builder()
                    .success(false)
                    .message("Camera không tồn tại")
                    .build());
        }

        List<DetectionResult> detections = aiDetectionStreamService.getLatestDetections(cameraId);

        double avgConfidence = detections.stream()
            .mapToDouble(DetectionResult::getConfidence)
            .average()
            .orElse(0.0);

        DetectionStreamResponse response = DetectionStreamResponse.builder()
            .cameraId(cameraId)
            .cameraName(camera.getName())
            .timestamp(LocalDateTime.now())
            .detections(detections)
            .objectCount(detections.size())
            .averageConfidence(avgConfidence)
            .build();

        return ResponseEntity.ok(ApiResponse.<DetectionStreamResponse>builder()
            .success(true)
            .data(response)
            .build());
    }

    @PostMapping("/monitor/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Start stream monitoring")
    public ResponseEntity<ApiResponse<String>> startMonitoring() {
        log.info("Starting stream monitoring");

        streamMonitorService.startMonitoring();

        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Đã bắt đầu stream monitoring")
            .build());
    }

    @PostMapping("/monitor/stop")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Stop stream monitoring")
    public ResponseEntity<ApiResponse<String>> stopMonitoring() {
        log.info("Stopping stream monitoring");

        streamMonitorService.stopMonitoring();

        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Đã dừng stream monitoring")
            .build());
    }

    @PostMapping("/restart/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Manually restart a stream")
    public ResponseEntity<ApiResponse<String>> restartStream(@PathVariable Long cameraId) {
        log.info("Manual restart for camera {}", cameraId);

        boolean restarted = streamMonitorService.manualRestartStream(cameraId);

        if (restarted) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Đã restart stream cho camera " + cameraId)
                .build());
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Không thể restart stream cho camera " + cameraId)
                    .build());
        }
    }

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Get health status for all streams")
    public ResponseEntity<ApiResponse<Map<Long, Boolean>>> getHealthStatus() {
        Map<Long, Boolean> healthStatus = streamMonitorService.getAllStreamHealthStatus();

        return ResponseEntity.ok(ApiResponse.<Map<Long, Boolean>>builder()
            .success(true)
            .data(healthStatus)
            .build());
    }
}
