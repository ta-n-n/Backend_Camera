package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.CameraHealthLog;
import com.example.camerasurveillancesystem.dto.response.CameraHealthLogResponse;
import org.springframework.stereotype.Component;

@Component
public class CameraHealthLogMapper {

    public CameraHealthLogResponse toResponse(CameraHealthLog log) {
        if (log == null) {
            return null;
        }

        return CameraHealthLogResponse.builder()
                .id(log.getId())
                .cameraId(log.getCamera() != null ? log.getCamera().getId() : null)
                .cameraName(log.getCamera() != null ? log.getCamera().getName() : null)
                .cameraCode(log.getCamera() != null ? log.getCamera().getCode() : null)
                .status(log.getStatus())
                .message(log.getMessage())
                .cpuUsage(log.getCpuUsage())
                .memoryUsage(log.getMemoryUsage())
                .diskUsage(log.getDiskUsage())
                .temperature(log.getTemperature())
                .bandwidth(log.getBandwidth())
                .errorCode(log.getErrorCode())
                .checkedAt(log.getCheckedAt())
                .build();
    }
}
