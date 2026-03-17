package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.CameraDeleteMultipleRequest;
import com.example.camerasurveillancesystem.dto.request.CameraHealthLogCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraHealthLogSearchRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.CameraHealthLogResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.service.CameraHealthLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/camera-health-logs")
@RequiredArgsConstructor
@Slf4j
public class CameraHealthLogController {

    private final CameraHealthLogService healthLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<CameraHealthLogResponse>> createHealthLog(
            @Valid @RequestBody CameraHealthLogCreateRequest request) {
        log.info("Request to create health log for camera ID: {}", request.getCameraId());
        
        CameraHealthLogResponse response = healthLogService.createHealthLog(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo health log thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraHealthLogResponse>> getHealthLogById(@PathVariable Long id) {
        log.info("Request to get health log by ID: {}", id);
        
        CameraHealthLogResponse response = healthLogService.getHealthLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CameraHealthLogResponse>>> getAllHealthLogs() {
        log.info("Request to get all health logs");
        
        List<CameraHealthLogResponse> logs = healthLogService.getAllHealthLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<List<CameraHealthLogResponse>>> getHealthLogsByCameraId(
            @PathVariable Long cameraId) {
        log.info("Request to get health logs for camera ID: {}", cameraId);
        
        List<CameraHealthLogResponse> logs = healthLogService.getHealthLogsByCameraId(cameraId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/camera/{cameraId}/latest")
    public ResponseEntity<ApiResponse<CameraHealthLogResponse>> getLatestHealthLogByCameraId(
            @PathVariable Long cameraId) {
        log.info("Request to get latest health log for camera ID: {}", cameraId);
        
        CameraHealthLogResponse response = healthLogService.getLatestHealthLogByCameraId(cameraId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<CameraHealthLogResponse>>> getHealthLogsByStatus(
            @PathVariable String status) {
        log.info("Request to get health logs by status: {}", status);
        
        List<CameraHealthLogResponse> logs = healthLogService.getHealthLogsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CameraHealthLogResponse>>> searchHealthLogs(
            @RequestBody CameraHealthLogSearchRequest request) {
        log.info("Request to search health logs with filters");
        
        PageResponse<CameraHealthLogResponse> response = healthLogService.searchHealthLogs(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHealthLog(@PathVariable Long id) {
        log.info("Request to delete health log ID: {}", id);
        
        healthLogService.deleteHealthLog(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa health log thành công", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleHealthLogs(
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to delete multiple health logs: {} logs", request.getIds().size());
        
        healthLogService.deleteMultipleHealthLogs(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thành công " + request.getIds().size() + " health log", null));
    }

    @DeleteMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<Void>> deleteHealthLogsByCameraId(@PathVariable Long cameraId) {
        log.info("Request to delete all health logs for camera ID: {}", cameraId);
        
        healthLogService.deleteHealthLogsByCameraId(cameraId);
        return ResponseEntity.ok(ApiResponse.success("Xóa tất cả health log của camera thành công", null));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> deleteOldHealthLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        log.info("Request to delete health logs before: {}", beforeDate);
        
        healthLogService.deleteOldHealthLogs(beforeDate);
        return ResponseEntity.ok(ApiResponse.success("Xóa health log cũ thành công", null));
    }
}
