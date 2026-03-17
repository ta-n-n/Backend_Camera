package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StreamDetectionConfigRequest {

    @NotNull(message = "Camera ID không được để trống")
    private Long cameraId;

    @Min(value = 0, message = "Confidence threshold phải từ 0.0 đến 1.0")
    @Max(value = 1, message = "Confidence threshold phải từ 0.0 đến 1.0")
    private Double confidenceThreshold;

    @Min(value = 1, message = "Frame skip phải lớn hơn 0")
    @Max(value = 60, message = "Frame skip không nên quá 60")
    private Integer frameSkip;

    private Boolean autoRestart;
}
