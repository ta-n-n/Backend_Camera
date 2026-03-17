package com.example.camerasurveillancesystem.dto.response;

import com.example.camerasurveillancesystem.ai.detector.DetectionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionStreamResponse {

    private Long cameraId;
    private String cameraName;
    private LocalDateTime timestamp;
    private List<DetectionResult> detections;
    private Integer objectCount;
    private Double averageConfidence;
}
