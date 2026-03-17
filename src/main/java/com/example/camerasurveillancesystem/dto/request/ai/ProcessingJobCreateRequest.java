package com.example.camerasurveillancesystem.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingJobCreateRequest {

    @NotBlank(message = "Job type is required")
    private String jobType; // VIDEO_ANALYSIS, FACE_RECOGNITION, MOTION_DETECTION, STORAGE_CLEANUP

    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    private String parameters; // JSON format for job-specific parameters

    private Integer maxRetries = 3;
}
