package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PtzMoveRequest {

    @Min(value = 1, message = "durationMs phải >= 1")
    @Max(value = 60000, message = "durationMs phải <= 60000")
    private Long durationMs;

    @Min(value = 1, message = "speedPercent phải >= 1")
    @Max(value = 100, message = "speedPercent phải <= 100")
    private Integer speedPercent;
}
