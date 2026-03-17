package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.CameraLocation;
import com.example.camerasurveillancesystem.dto.mapper.CameraLocationMapper;
import com.example.camerasurveillancesystem.dto.request.CameraLocationCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraLocationUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraLocationResponse;
import com.example.camerasurveillancesystem.exception.*;
import com.example.camerasurveillancesystem.repository.CameraLocationRepository;
import com.example.camerasurveillancesystem.service.CameraLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CameraLocationServiceImpl implements CameraLocationService {

    private final CameraLocationRepository locationRepository;
    private final CameraLocationMapper locationMapper;

    @Override
    public CameraLocationResponse createLocation(CameraLocationCreateRequest request) {
        log.info("Creating new camera location: {}", request.getName());

        CameraLocation location = new CameraLocation();
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setCity(request.getCity());
        location.setDistrict(request.getDistrict());
        location.setDescription(request.getDescription());

        CameraLocation savedLocation = locationRepository.save(location);
        log.info("Camera location created successfully with ID: {}", savedLocation.getId());

        return locationMapper.toResponse(savedLocation);
    }

    @Override
    public CameraLocationResponse updateLocation(Long id, CameraLocationUpdateRequest request) {
        log.info("Updating camera location with ID: {}", id);

        CameraLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.LOCATION_NOT_FOUND,
                    "Không tìm thấy vị trí với ID: " + id
                ));

        if (request.getName() != null) {
            location.setName(request.getName());
        }
        if (request.getAddress() != null) {
            location.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            location.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            location.setLongitude(request.getLongitude());
        }
        if (request.getCity() != null) {
            location.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            location.setDistrict(request.getDistrict());
        }
        if (request.getDescription() != null) {
            location.setDescription(request.getDescription());
        }

        CameraLocation updatedLocation = locationRepository.save(location);
        log.info("Camera location updated successfully with ID: {}", updatedLocation.getId());

        return locationMapper.toResponse(updatedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public CameraLocationResponse getLocationById(Long id) {
        log.info("Getting camera location by ID: {}", id);

        CameraLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.LOCATION_NOT_FOUND,
                    "Không tìm thấy vị trí với ID: " + id
                ));

        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraLocationResponse> getAllLocations() {
        log.info("Getting all camera locations");

        List<CameraLocation> locations = locationRepository.findAll();

        return locations.stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLocation(Long id) {
        log.info("Deleting camera location with ID: {}", id);

        CameraLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.LOCATION_NOT_FOUND,
                    "Không tìm thấy vị trí với ID: " + id
                ));

        // Check if location has cameras
        if (location.getCameras() != null && !location.getCameras().isEmpty()) {
            throw new BusinessException(
                ErrorCode.RESOURCE_IN_USE,
                "Không thể xóa vị trí này vì đang có " + location.getCameras().size() + " camera sử dụng"
            );
        }

        locationRepository.deleteById(id);
        log.info("Camera location deleted successfully with ID: {}", id);
    }

    @Override
    public void deleteMultipleLocations(List<Long> ids) {
        log.info("Deleting multiple camera locations with IDs: {}", ids);

        List<CameraLocation> locations = locationRepository.findAllById(ids);

        if (locations.size() != ids.size()) {
            throw new ResourceNotFoundException(
                ErrorCode.LOCATION_NOT_FOUND,
                "Một số vị trí không tồn tại trong hệ thống"
            );
        }

        // Check if any location has cameras
        for (CameraLocation location : locations) {
            if (location.getCameras() != null && !location.getCameras().isEmpty()) {
                throw new BusinessException(
                    ErrorCode.RESOURCE_IN_USE,
                    "Vị trí '" + location.getName() + "' đang có camera sử dụng, không thể xóa"
                );
            }
        }

        locationRepository.deleteAllById(ids);
        log.info("Deleted {} camera locations successfully", ids.size());
    }
}
