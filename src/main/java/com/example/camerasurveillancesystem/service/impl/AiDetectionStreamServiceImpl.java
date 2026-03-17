package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.ai.detector.DetectionResult;
import com.example.camerasurveillancesystem.ai.detector.ObjectDetector;
import com.example.camerasurveillancesystem.domain.AiEvent;
import com.example.camerasurveillancesystem.domain.AiEventObject;
import com.example.camerasurveillancesystem.domain.AiModel;
import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.dto.request.StreamAutoAlertConfigRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertCreateRequest;
import com.example.camerasurveillancesystem.dto.response.StreamAutoAlertConfigResponse;
import com.example.camerasurveillancesystem.repository.AiEventRepository;
import com.example.camerasurveillancesystem.repository.AiModelRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.AiDetectionStreamService;
import com.example.camerasurveillancesystem.service.AlertService;
import com.example.camerasurveillancesystem.service.MediaMTXService;
import com.example.camerasurveillancesystem.service.VideoStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDetectionStreamServiceImpl implements AiDetectionStreamService {

    private final VideoStreamService videoStreamService;
    private final ObjectDetector objectDetector;
    private final CameraRepository cameraRepository;
    private final AiModelRepository aiModelRepository;
    private final AiEventRepository aiEventRepository;
    private final AlertService alertService;
    private final MediaMTXService mediaMTXService;
    private final com.example.camerasurveillancesystem.websocket.WebSocketEventPublisher webSocketPublisher;

    private final Map<Long, DetectionContext> detectionContexts = new ConcurrentHashMap<>();
    private final Map<Long, List<DetectionResult>> latestDetections = new ConcurrentHashMap<>();
    private final Map<Long, String> lastStartErrors = new ConcurrentHashMap<>();
    private final Map<Long, AutoAlertConfig> autoAlertConfigs = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastAutoAlertTimestamps = new ConcurrentHashMap<>();

    // Default configuration
    private static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.5;
    private static final int DEFAULT_FRAME_SKIP = 5; // Phát hiện mỗi 5 frames
    private static final double DEFAULT_AUTO_ALERT_MIN_CONFIDENCE = 0.6;
    private static final int DEFAULT_AUTO_ALERT_COOLDOWN_SECONDS = 30;
    private static final String DEFAULT_AUTO_ALERT_TYPE = "INTRUSION";
    private static final String DEFAULT_AUTO_ALERT_SEVERITY = "MEDIUM";
    private static final String SNAPSHOT_BASE_DIR = "uploads/snapshots/ai";
    private static final String SNAPSHOT_URL_PREFIX = "/api/v1/files/snapshots/ai/";

    @Override
    @Transactional
    public boolean startDetection(Long cameraId) {
        log.info("Starting AI detection for camera {}", cameraId);
        lastStartErrors.remove(cameraId);

        // Kiểm tra camera tồn tại
        Optional<Camera> cameraOpt = cameraRepository.findById(cameraId);
        if (cameraOpt.isEmpty()) {
            log.error("Camera {} not found", cameraId);
            lastStartErrors.put(cameraId, "Camera không tồn tại");
            return false;
        }

        Camera camera = cameraOpt.get();
        
        String rtspUrl = camera.getRtspUrl() != null ? camera.getRtspUrl().trim() : null;

        // Kiểm tra có RTSP URL không
        if (rtspUrl == null || rtspUrl.isBlank()) {
            log.error("Camera {} has no RTSP URL", cameraId);
            lastStartErrors.put(cameraId, "Camera chưa có RTSP URL");
            return false;
        }

        String detectionRtspUrl = rtspUrl;
        boolean rtspReachable;

        if (mediaMTXService.isEnabled() && camera.getCode() != null && !camera.getCode().isBlank()) {
            String relayRtspUrl = mediaMTXService.getRtspUrl(camera.getCode());
            try {
                mediaMTXService.registerCamera(camera.getCode(), rtspUrl);
            } catch (Exception e) {
                log.warn("Could not register camera {} on MediaMTX: {}", cameraId, e.getMessage());
            }

            log.info("Trying MediaMTX relay first for camera {}: {}", cameraId, relayRtspUrl);
            rtspReachable = videoStreamService.testRtspConnection(relayRtspUrl, 6);
            if (rtspReachable) {
                detectionRtspUrl = relayRtspUrl;
                log.info("Using MediaMTX relay for camera {}: {}", cameraId, detectionRtspUrl);
            } else {
                log.warn("Relay RTSP failed for camera {}, fallback to direct RTSP", cameraId);
                rtspReachable = videoStreamService.testRtspConnection(rtspUrl, 6);
                detectionRtspUrl = rtspUrl;
            }
        } else {
            // Kiểm tra RTSP direct khi không dùng MediaMTX
            rtspReachable = videoStreamService.testRtspConnection(rtspUrl, 6);
        }

        // Kiểm tra RTSP có truy cập được không trước khi load model
        if (!rtspReachable) {
            log.error("Camera {} RTSP is not reachable", cameraId);
            lastStartErrors.put(cameraId,
                    "Không kết nối được RTSP từ backend. URL test: " + detectionRtspUrl);
            return false;
        }

        // Kiểm tra model đã load chưa
        if (!objectDetector.isModelLoaded()) {
            try {
                log.info("Loading AI model...");
                objectDetector.loadModel();
            } catch (Exception e) {
                log.error("Failed to load AI model: {}", e.getMessage(), e);
                lastStartErrors.put(cameraId, "Không load được model AI: " + e.getMessage());
                return false;
            }
        }

        // Tạo detection context
        DetectionContext context = new DetectionContext(
            cameraId,
            DEFAULT_CONFIDENCE_THRESHOLD,
            DEFAULT_FRAME_SKIP
        );
        detectionContexts.put(cameraId, context);

        // Bắt đầu video stream với frame processor
        boolean started = videoStreamService.startStream(
            cameraId,
            detectionRtspUrl,
            frame -> processFrame(cameraId, frame, context)
        );

        if (started) {
            log.info("AI detection started successfully for camera {}", cameraId);
            lastStartErrors.remove(cameraId);
        } else {
            log.error("Failed to start AI detection for camera {}", cameraId);
            detectionContexts.remove(cameraId);
            lastStartErrors.put(cameraId, "Mở video stream thất bại từ RTSP URL");
        }

        return started;
    }

    private void processFrame(Long cameraId, Mat frame, DetectionContext context) {
        try {
            // Skip frames để giảm CPU usage
            context.incrementFrameCount();
            if (context.getFrameCount() % context.getFrameSkip() != 0) {
                return;
            }

            // Phát hiện objects
            List<DetectionResult> detections = objectDetector.detect(frame);

            // Lọc theo confidence threshold
            List<DetectionResult> filteredDetections = detections.stream()
                .filter(d -> d.getConfidence() >= context.getConfidenceThreshold())
                .toList();

            if (!filteredDetections.isEmpty()) {
                log.debug("Detected {} objects in camera {}", filteredDetections.size(), cameraId);
                
                // Lưu latest detections
                latestDetections.put(cameraId, filteredDetections);
                
                // Publish real-time detection qua WebSocket cho FE
                publishDetectionToWebSocket(cameraId, filteredDetections);
                
                // Lưu vào database (async để không block stream)
                saveDetectionEvent(cameraId, filteredDetections, frame);
            }

        } catch (Exception e) {
            log.error("Error processing frame for camera {}: {}", cameraId, e.getMessage(), e);
        }
    }

    /**
     * Publish detection results qua WebSocket cho Frontend real-time
     */
    private void publishDetectionToWebSocket(Long cameraId, List<DetectionResult> detections) {
        try {
            Map<String, Object> detectionData = new HashMap<>();
            detectionData.put("cameraId", cameraId);
            detectionData.put("timestamp", System.currentTimeMillis());
            detectionData.put("detectionCount", detections.size());
            
            // Serialize detections với bounding boxes
            List<Map<String, Object>> detectionsList = new ArrayList<>();
            for (DetectionResult detection : detections) {
                Map<String, Object> det = new HashMap<>();
                det.put("objectType", detection.getObjectType());
                det.put("label", detection.getLabel());
                det.put("confidence", detection.getConfidence());
                
                if (detection.getBoundingBox() != null) {
                    Map<String, Object> bbox = new HashMap<>();
                    bbox.put("x", detection.getBoundingBox().getX());
                    bbox.put("y", detection.getBoundingBox().getY());
                    bbox.put("width", detection.getBoundingBox().getWidth());
                    bbox.put("height", detection.getBoundingBox().getHeight());
                    det.put("boundingBox", bbox);
                }
                
                detectionsList.add(det);
            }
            detectionData.put("detections", detectionsList);
            
            // Send to WebSocket topic
            webSocketPublisher.publishCameraStreamUpdate(cameraId, detectionData);
            
        } catch (Exception e) {
            log.error("Error publishing detection to WebSocket for camera {}: {}", cameraId, e.getMessage());
        }
    }

    @Transactional
    protected void saveDetectionEvent(Long cameraId, List<DetectionResult> detections, Mat frame) {
        try {
            Camera camera = cameraRepository.findById(cameraId).orElse(null);
            if (camera == null) {
                return;
            }

            // Lấy AI model (giả sử dùng model mặc định, có thể cải thiện sau)
            AiModel model = aiModelRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .findFirst()
                .orElse(null);

            if (model == null) {
                log.warn("No active AI model found, skipping event save");
                return;
            }

            // Tạo AI event
            AiEvent event = new AiEvent();
            event.setCamera(camera);
            event.setModel(model);
            event.setEventType("OBJECT_DETECTION");
            event.setDetectedAt(LocalDateTime.now());
            
            // Tính confidence trung bình
            double avgConfidence = detections.stream()
                .mapToDouble(DetectionResult::getConfidence)
                .average()
                .orElse(0.0);
            event.setConfidenceScore(avgConfidence);
            
            // Tạo metadata summary
            Map<String, Long> objectCounts = new HashMap<>();
            for (DetectionResult detection : detections) {
                String objectType = detection.getObjectType();
                objectCounts.put(objectType, objectCounts.getOrDefault(objectType, 0L) + 1);
            }
            event.setMetadata(objectCounts.toString());

            // Lưu event objects
            List<AiEventObject> eventObjects = new ArrayList<>();
            for (DetectionResult detection : detections) {
                AiEventObject eventObject = new AiEventObject();
                eventObject.setAiEvent(event);
                eventObject.setObjectType(detection.getObjectType());
                eventObject.setConfidence(detection.getConfidence());
                eventObject.setLabel(detection.getLabel());
                
                // Set bounding box coordinates
                if (detection.getBoundingBox() != null) {
                    eventObject.setBoundingBoxX(detection.getBoundingBox().getX());
                    eventObject.setBoundingBoxY(detection.getBoundingBox().getY());
                    eventObject.setBoundingBoxWidth(detection.getBoundingBox().getWidth());
                    eventObject.setBoundingBoxHeight(detection.getBoundingBox().getHeight());
                }
                
                eventObjects.add(eventObject);
            }
            event.setDetectedObjects(eventObjects);

            // Lưu vào database
            aiEventRepository.save(event);
            triggerAutoAlert(cameraId, event, detections, frame);
            
            log.debug("Saved AI event for camera {} with {} objects", cameraId, detections.size());

        } catch (Exception e) {
            log.error("Error saving detection event for camera {}: {}", cameraId, e.getMessage(), e);
        }
    }

    @Override
    public void stopDetection(Long cameraId) {
        log.info("Stopping AI detection for camera {}", cameraId);
        
        videoStreamService.stopStream(cameraId);
        detectionContexts.remove(cameraId);
        latestDetections.remove(cameraId);
        lastStartErrors.remove(cameraId);
        
        log.info("AI detection stopped for camera {}", cameraId);
    }

    @Override
    @Transactional(readOnly = true)
    public int startDetectionForAllActiveCameras() {
        log.info("Starting AI detection for all active cameras");
        
        List<Camera> activeCameras = cameraRepository.findByStatus("ACTIVE");
        int successCount = 0;
        
        for (Camera camera : activeCameras) {
            if (camera.getRtspUrl() != null && !camera.getRtspUrl().isEmpty()) {
                if (startDetection(camera.getId())) {
                    successCount++;
                }
            }
        }
        
        log.info("Started AI detection for {} cameras", successCount);
        return successCount;
    }

    @Override
    public void stopAllDetections() {
        log.info("Stopping all AI detections");
        
        Set<Long> cameraIds = new HashSet<>(detectionContexts.keySet());
        cameraIds.forEach(this::stopDetection);
        
        log.info("All AI detections stopped");
    }

    @Override
    public boolean isDetectionRunning(Long cameraId) {
        return videoStreamService.isStreamRunning(cameraId);
    }

    @Override
    public List<DetectionResult> getLatestDetections(Long cameraId) {
        return latestDetections.getOrDefault(cameraId, Collections.emptyList());
    }

    @Override
    public void setConfidenceThreshold(Long cameraId, Double threshold) {
        DetectionContext context = detectionContexts.get(cameraId);
        if (context != null) {
            context.setConfidenceThreshold(threshold);
            log.info("Updated confidence threshold for camera {} to {}", cameraId, threshold);
        }
    }

    @Override
    public void setFrameSkip(Long cameraId, int skipFrames) {
        DetectionContext context = detectionContexts.get(cameraId);
        if (context != null) {
            context.setFrameSkip(skipFrames);
            log.info("Updated frame skip for camera {} to {}", cameraId, skipFrames);
        }
    }

    @Override
    public String getLastStartError(Long cameraId) {
        return lastStartErrors.get(cameraId);
    }

    @Override
    public void configureAutoAlert(StreamAutoAlertConfigRequest request) {
        cameraRepository.findById(request.getCameraId())
                .orElseThrow(() -> new IllegalArgumentException("Camera không tồn tại: " + request.getCameraId()));

        AutoAlertConfig config = autoAlertConfigs.computeIfAbsent(
                request.getCameraId(),
                key -> new AutoAlertConfig()
        );

        config.enabled = Boolean.TRUE.equals(request.getEnabled());
        config.minConfidence = request.getMinConfidence() != null
                ? Math.max(0.0, Math.min(1.0, request.getMinConfidence()))
                : DEFAULT_AUTO_ALERT_MIN_CONFIDENCE;
        config.cooldownSeconds = request.getCooldownSeconds() != null
                ? Math.max(1, request.getCooldownSeconds())
                : DEFAULT_AUTO_ALERT_COOLDOWN_SECONDS;
        config.alertType = normalizeAlertType(request.getAlertType());
        config.severity = normalizeSeverity(request.getSeverity());
        config.objectTypes = normalizeObjectTypes(request.getObjectTypes());

        log.info("Auto-alert config updated for camera {}: enabled={}, minConfidence={}, cooldownSeconds={}, alertType={}, severity={}, objectTypes={}",
                request.getCameraId(),
                config.enabled,
                config.minConfidence,
                config.cooldownSeconds,
                config.alertType,
                config.severity,
                config.objectTypes);
    }

    @Override
    public StreamAutoAlertConfigResponse getAutoAlertConfig(Long cameraId) {
        AutoAlertConfig config = autoAlertConfigs.computeIfAbsent(cameraId, key -> new AutoAlertConfig());
        return StreamAutoAlertConfigResponse.builder()
                .cameraId(cameraId)
                .enabled(config.enabled)
                .minConfidence(config.minConfidence)
                .cooldownSeconds(config.cooldownSeconds)
                .alertType(config.alertType)
                .severity(config.severity)
                .objectTypes(new ArrayList<>(config.objectTypes))
                .lastAlertAt(lastAutoAlertTimestamps.get(cameraId))
                .build();
    }

    private void triggerAutoAlert(Long cameraId, AiEvent event, List<DetectionResult> detections, Mat frame) {
        AutoAlertConfig config = autoAlertConfigs.get(cameraId);
        if (config == null || !config.enabled) {
            return;
        }

        List<DetectionResult> candidates = detections.stream()
                .filter(detection -> detection.getConfidence() >= config.minConfidence)
                .filter(detection -> config.objectTypes.isEmpty() || config.objectTypes.contains(detection.getObjectType()))
                .toList();

        if (candidates.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAlertAt = lastAutoAlertTimestamps.get(cameraId);
        if (lastAlertAt != null) {
            long secondsSinceLastAlert = ChronoUnit.SECONDS.between(lastAlertAt, now);
            if (secondsSinceLastAlert < config.cooldownSeconds) {
                return;
            }
        }

        try {
            Map<String, Long> objectCounts = new HashMap<>();
            for (DetectionResult detection : candidates) {
                objectCounts.put(detection.getObjectType(), objectCounts.getOrDefault(detection.getObjectType(), 0L) + 1L);
            }

            String snapshotUrl = saveSnapshotFromFrame(cameraId, frame);
            event.setSnapshotPath(snapshotUrl);
            aiEventRepository.save(event);

            AlertCreateRequest alertRequest = AlertCreateRequest.builder()
                    .cameraId(cameraId)
                    .aiEventId(event.getId())
                    .alertType(config.alertType)
                    .severity(config.severity)
                    .title("AI Detection Alert - Camera " + event.getCamera().getCode())
                    .description("Phát hiện " + candidates.size() + " đối tượng: " + objectCounts)
                    .snapshotUrl(snapshotUrl)
                    .metadata(objectCounts.toString())
                    .build();

            alertService.createAlert(alertRequest);
            lastAutoAlertTimestamps.put(cameraId, now);

            log.info("Auto alert created for camera {} from aiEvent {} with {} detections", cameraId, event.getId(), candidates.size());
        } catch (Exception e) {
            log.error("Failed to auto-create alert for camera {}: {}", cameraId, e.getMessage(), e);
        }
    }

    private String saveSnapshotFromFrame(Long cameraId, Mat frame) {
        if (frame == null || frame.empty()) {
            return null;
        }

        try {
            Path snapshotDir = Paths.get(SNAPSHOT_BASE_DIR);
            Files.createDirectories(snapshotDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = "cam_" + cameraId + "_" + timestamp + ".jpg";
            Path filePath = snapshotDir.resolve(fileName);

            boolean saved = Imgcodecs.imwrite(filePath.toString(), frame);
            if (!saved) {
                log.warn("Could not write snapshot image for camera {}", cameraId);
                return null;
            }

            return SNAPSHOT_URL_PREFIX + fileName;
        } catch (Exception e) {
            log.error("Failed to save snapshot for camera {}: {}", cameraId, e.getMessage(), e);
            return null;
        }
    }

    private String normalizeAlertType(String alertType) {
        if (alertType == null || alertType.isBlank()) {
            return DEFAULT_AUTO_ALERT_TYPE;
        }
        return alertType.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return DEFAULT_AUTO_ALERT_SEVERITY;
        }
        String normalized = severity.trim().toUpperCase(Locale.ROOT);
        Set<String> allowed = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
        return allowed.contains(normalized) ? normalized : DEFAULT_AUTO_ALERT_SEVERITY;
    }

    private Set<String> normalizeObjectTypes(List<String> objectTypes) {
        if (objectTypes == null || objectTypes.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalized = new HashSet<>();
        for (String objectType : objectTypes) {
            if (objectType != null && !objectType.isBlank()) {
                normalized.add(objectType.trim().toUpperCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up AiDetectionStreamService");
        stopAllDetections();
    }

    /**
     * Context class để lưu thông tin detection
     */
    private static class DetectionContext {
        private final Long cameraId;
        private volatile double confidenceThreshold;
        private volatile int frameSkip;
        private volatile long frameCount;

        public DetectionContext(Long cameraId, double confidenceThreshold, int frameSkip) {
            this.cameraId = cameraId;
            this.confidenceThreshold = confidenceThreshold;
            this.frameSkip = frameSkip;
            this.frameCount = 0;
        }

        public Long getCameraId() {
            return cameraId;
        }

        public double getConfidenceThreshold() {
            return confidenceThreshold;
        }

        public void setConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
        }

        public int getFrameSkip() {
            return frameSkip;
        }

        public void setFrameSkip(int frameSkip) {
            this.frameSkip = frameSkip;
        }

        public long getFrameCount() {
            return frameCount;
        }

        public void incrementFrameCount() {
            this.frameCount++;
        }
    }

    private static class AutoAlertConfig {
        private boolean enabled = false;
        private double minConfidence = DEFAULT_AUTO_ALERT_MIN_CONFIDENCE;
        private int cooldownSeconds = DEFAULT_AUTO_ALERT_COOLDOWN_SECONDS;
        private String alertType = DEFAULT_AUTO_ALERT_TYPE;
        private String severity = DEFAULT_AUTO_ALERT_SEVERITY;
        private Set<String> objectTypes = new HashSet<>();
    }
}
