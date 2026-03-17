package com.example.camerasurveillancesystem.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiModelUpdateRequest {

    private String name;

    private String version;

    private String type;

    private String description;

    private String modelPath;

    private String configPath;

    private Double accuracy;

    private Boolean isActive;
}
