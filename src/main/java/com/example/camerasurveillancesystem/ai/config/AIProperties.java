package com.example.camerasurveillancesystem.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình AI properties
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AIProperties {

    private YoloConfig yolo = new YoloConfig();
    private VideoConfig video = new VideoConfig();
    private ProcessingConfig processing = new ProcessingConfig();

    @Data
    public static class YoloConfig {
        private ModelConfig model = new ModelConfig();
        private float confidenceThreshold = 0.5f;
        private float nmsThreshold = 0.4f;
        private int inputSize = 640;
        private DetectionFilter detectionFilter = new DetectionFilter();
    }

    @Data
    public static class DetectionFilter {
        private boolean enableFilter = true;
        // Danh sách các classes cần phát hiện (persons và vehicles)
        private java.util.List<String> allowedClasses = java.util.Arrays.asList(
            "person", "bicycle", "car", "motorcycle", "bus", "train", "truck"
        );
    }

    @Data
    public static class ModelConfig {
        private String path = "models/yolov8n.onnx";
        private String type = "yolov8";
        private String version = "8n";
    }

    @Data
    public static class VideoConfig {
        private int fps = 5;                    // Số frame xử lý mỗi giây
        private String outputPath = "output";   // Thư mục lưu video output
        private boolean saveDetectionVideo = false;
        private int maxDuration = 3600;         // Thời gian tối đa (giây)
    }

    @Data
    public static class ProcessingConfig {
        private int threadPoolSize = 4;
        private int queueCapacity = 100;
        private int batchSize = 10;
        private boolean enableAsync = true;
    }
}
