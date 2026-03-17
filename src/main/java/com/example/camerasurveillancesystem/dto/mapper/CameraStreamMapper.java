package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.CameraStream;
import com.example.camerasurveillancesystem.dto.response.CameraStreamResponse;
import org.springframework.stereotype.Component;

@Component
public class CameraStreamMapper {

    public CameraStreamResponse toResponse(CameraStream stream) {
        if (stream == null) {
            return null;
        }

        return CameraStreamResponse.builder()
                .id(stream.getId())
                .cameraId(stream.getCamera() != null ? stream.getCamera().getId() : null)
                .cameraName(stream.getCamera() != null ? stream.getCamera().getName() : null)
                .cameraCode(stream.getCamera() != null ? stream.getCamera().getCode() : null)
                .streamType(stream.getStreamType())
                .streamUrl(stream.getStreamUrl())
                .protocol(stream.getProtocol())
                .quality(stream.getQuality())
                .resolution(stream.getResolution())
                .bitrate(stream.getBitrate())
                .frameRate(stream.getFrameRate())
                .isActive(stream.getIsActive())
                .createdAt(stream.getCreatedAt())
                .updatedAt(stream.getUpdatedAt())
                .build();
    }
}
