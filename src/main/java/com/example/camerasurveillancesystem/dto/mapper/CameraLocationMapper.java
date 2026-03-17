package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.CameraLocation;
import com.example.camerasurveillancesystem.dto.response.CameraLocationResponse;
import org.springframework.stereotype.Component;

@Component
public class CameraLocationMapper {

    public CameraLocationResponse toResponse(CameraLocation location) {
        if (location == null) {
            return null;
        }

        return CameraLocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .city(location.getCity())
                .district(location.getDistrict())
                .description(location.getDescription())
                .cameraCount(location.getCameras() != null ? location.getCameras().size() : 0)
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}
