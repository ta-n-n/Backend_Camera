package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraGroup;
import com.example.camerasurveillancesystem.dto.response.CameraResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CameraMapper {

    public CameraResponse toResponse(Camera camera) {
        if (camera == null) {
            return null;
        }

        CameraResponse response = CameraResponse.builder()
                .id(camera.getId())
                .name(camera.getName())
                .code(camera.getCode())
                .model(camera.getModel())
                .manufacturer(camera.getManufacturer())
                .rtspUrl(camera.getRtspUrl())
                .snapshotUrl(camera.getSnapshotUrl())
                .status(camera.getStatus())
                .resolution(camera.getResolution())
                .frameRate(camera.getFrameRate())
                .description(camera.getDescription())
                .createdAt(camera.getCreatedAt())
                .updatedAt(camera.getUpdatedAt())
                .build();

        // Map location
        if (camera.getLocation() != null) {
            response.setLocation(CameraResponse.CameraLocationResponse.builder()
                    .id(camera.getLocation().getId())
                    .name(camera.getLocation().getName())
                    .address(camera.getLocation().getAddress())
                    .latitude(camera.getLocation().getLatitude())
                    .longitude(camera.getLocation().getLongitude())
                    .city(camera.getLocation().getCity())
                    .district(camera.getLocation().getDistrict())
                    .build());
        }

        // Map groups
        if (camera.getGroups() != null && !camera.getGroups().isEmpty()) {
            response.setGroups(camera.getGroups().stream()
                    .map(group -> CameraResponse.CameraGroupResponse.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .description(group.getDescription())
                            .build())
                    .collect(Collectors.toSet()));
        }

        return response;
    }
}
