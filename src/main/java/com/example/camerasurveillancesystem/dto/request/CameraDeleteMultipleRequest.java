package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraDeleteMultipleRequest {

    @NotEmpty(message = "Danh sách ID không được để trống")
    private List<Long> ids;
}
