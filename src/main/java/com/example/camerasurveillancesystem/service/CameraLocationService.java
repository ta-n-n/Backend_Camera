package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.CameraLocationCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraLocationUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraLocationResponse;

import java.util.List;

public interface CameraLocationService {

    CameraLocationResponse createLocation(CameraLocationCreateRequest request);

    CameraLocationResponse updateLocation(Long id, CameraLocationUpdateRequest request);

    CameraLocationResponse getLocationById(Long id);

    List<CameraLocationResponse> getAllLocations();

    void deleteLocation(Long id);

    void deleteMultipleLocations(List<Long> ids);
}
