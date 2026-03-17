package com.example.camerasurveillancesystem.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiModelResponse {

    private Long id;
    private String name;
    private String version;
    private String type;
    private String description;
    private String modelPath;
    private String configPath;
    private Double accuracy;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
