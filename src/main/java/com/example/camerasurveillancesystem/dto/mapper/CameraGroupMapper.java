package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.CameraGroup;
import com.example.camerasurveillancesystem.dto.response.CameraGroupResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CameraGroupMapper {

    public CameraGroupResponse toResponse(CameraGroup group) {
        if (group == null) {
            return null;
        }

        CameraGroupResponse response = CameraGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .cameraCount(group.getCameras() != null ? group.getCameras().size() : 0)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();

        // Map cameras
        if (group.getCameras() != null && !group.getCameras().isEmpty()) {
            response.setCameras(group.getCameras().stream()
                    .map(camera -> CameraGroupResponse.CameraBasicInfo.builder()
                            .id(camera.getId())
                            .name(camera.getName())
                            .code(camera.getCode())
                            .status(camera.getStatus())
                            .build())
                    .collect(Collectors.toSet()));
        }

        return response;
    }
}
