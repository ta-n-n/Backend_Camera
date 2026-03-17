package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraHealthLogCreateRequest {

    @NotNull(message = "Camera ID không được để trống")
    private Long cameraId;

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "ONLINE|OFFLINE|ERROR|WARNING", message = "Trạng thái phải là ONLINE, OFFLINE, ERROR hoặc WARNING")
    private String status;

    @Size(max = 1000, message = "Thông điệp không được vượt quá 1000 ký tự")
    private String message;

    private Integer cpuUsage;

    private Integer memoryUsage;

    private Integer diskUsage;

    private Integer temperature;

    private Integer bandwidth;

    @Size(max = 50, message = "Mã lỗi không được vượt quá 50 ký tự")
    private String errorCode;
}
