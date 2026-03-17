package com.example.camerasurveillancesystem.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiModelCreateRequest {

    @NotBlank(message = "Model name is required")
    private String name;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Model type is required")
    private String type; // OBJECT_DETECTION, FACE_RECOGNITION, MOTION_DETECTION, LICENSE_PLATE

    private String description;

    private String modelPath;

    private String configPath;

    private Double accuracy;

    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
}
