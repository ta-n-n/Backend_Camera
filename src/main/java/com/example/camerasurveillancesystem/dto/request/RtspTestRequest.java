package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtspTestRequest {

    @NotBlank(message = "RTSP URL không được để trống")
    @Size(max = 500, message = "RTSP URL không được vượt quá 500 ký tự")
    private String rtspUrl;

    /** Timeout tính bằng giây, mặc định 5s */
    private int timeoutSeconds = 5;
}
