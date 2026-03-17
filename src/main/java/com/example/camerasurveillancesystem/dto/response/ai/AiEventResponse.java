package com.example.camerasurveillancesystem.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiEventResponse {

    private Long id;
    private Long cameraId;
    private String cameraName;
    private Long modelId;
    private String modelName;
    private String eventType;
    private Double confidenceScore;
    private String snapshotPath;
    private String metadata;
    private LocalDateTime detectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AiEventObjectResponse> detectedObjects;
}
