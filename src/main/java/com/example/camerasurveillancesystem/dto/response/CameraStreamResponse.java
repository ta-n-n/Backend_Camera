package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraStreamResponse {

    private Long id;
    private Long cameraId;
    private String cameraName;
    private String cameraCode;
    private String streamType;
    private String streamUrl;
    private String protocol;
    private String quality;
    private String resolution;
    private Integer bitrate;
    private Integer frameRate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
