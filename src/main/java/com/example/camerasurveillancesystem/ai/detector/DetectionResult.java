package com.example.camerasurveillancesystem.ai.detector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kết quả detection từ YOLOv8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionResult {

    private String objectType;      // PERSON, CAR, TRUCK, etc.
    private Double confidence;      // Độ tin cậy 0.0 - 1.0
    private String label;           // Label chi tiết
    private BoundingBox boundingBox;
    private List<String> attributes; // Thuộc tính bổ sung

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoundingBox {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }
}
