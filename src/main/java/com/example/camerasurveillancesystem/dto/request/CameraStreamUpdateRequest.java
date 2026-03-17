package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraStreamUpdateRequest {

    @Pattern(regexp = "LIVE|PLAYBACK|SNAPSHOT", message = "Loại stream phải là LIVE, PLAYBACK hoặc SNAPSHOT")
    private String streamType;

    @Size(max = 500, message = "Stream URL không được vượt quá 500 ký tự")
    private String streamUrl;

    @Size(max = 20, message = "Protocol không được vượt quá 20 ký tự")
    @Pattern(regexp = "RTSP|RTMP|HLS|WebRTC", message = "Protocol phải là RTSP, RTMP, HLS hoặc WebRTC")
    private String protocol;

    @Pattern(regexp = "HIGH|MEDIUM|LOW", message = "Chất lượng phải là HIGH, MEDIUM hoặc LOW")
    private String quality;

    @Size(max = 50, message = "Độ phân giải không được vượt quá 50 ký tự")
    private String resolution;

    private Integer bitrate;

    private Integer frameRate;

    private Boolean isActive;
}
