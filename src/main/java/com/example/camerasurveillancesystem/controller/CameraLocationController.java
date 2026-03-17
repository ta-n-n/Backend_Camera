package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.CameraDeleteMultipleRequest;
import com.example.camerasurveillancesystem.dto.request.CameraLocationCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraLocationUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.CameraLocationResponse;
import com.example.camerasurveillancesystem.service.CameraLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camera-locations")
@RequiredArgsConstructor
@Slf4j
public class CameraLocationController {

    private final CameraLocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CameraLocationResponse>> createLocation(
            @Valid @RequestBody CameraLocationCreateRequest request) {
        log.info("Request to create camera location: {}", request.getName());
        
        CameraLocationResponse response = locationService.createLocation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo vị trí thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraLocationResponse>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody CameraLocationUpdateRequest request) {
        log.info("Request to update camera location ID: {}", id);
        
        CameraLocationResponse response = locationService.updateLocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vị trí thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraLocationResponse>> getLocationById(@PathVariable Long id) {
        log.info("Request to get camera location by ID: {}", id);
        
        CameraLocationResponse response = locationService.getLocationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CameraLocationResponse>>> getAllLocations() {
        log.info("Request to get all camera locations");
        
        List<CameraLocationResponse> locations = locationService.getAllLocations();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable Long id) {
        log.info("Request to delete camera location ID: {}", id);
        
        locationService.deleteLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vị trí thành công", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleLocations(
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to delete multiple camera locations: {} locations", request.getIds().size());
        
        locationService.deleteMultipleLocations(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thành công " + request.getIds().size() + " vị trí", null));
    }
}
