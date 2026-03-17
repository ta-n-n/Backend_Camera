package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StreamAutoAlertConfigRequest {

    @NotNull(message = "Camera ID không được để trống")
    private Long cameraId;

    @NotNull(message = "enabled không được để trống")
    private Boolean enabled;

    @DecimalMin(value = "0.0", message = "minConfidence phải từ 0.0 đến 1.0")
    @DecimalMax(value = "1.0", message = "minConfidence phải từ 0.0 đến 1.0")
    private Double minConfidence;

    @Min(value = 1, message = "cooldownSeconds phải >= 1")
    @Max(value = 3600, message = "cooldownSeconds không nên quá 3600")
    private Integer cooldownSeconds;

    private String alertType;
    private String severity;

    // Lọc theo objectType nội bộ: PERSON, VEHICLE, ANIMAL, OBJECT
    private List<String> objectTypes;
}
