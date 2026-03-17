package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.CameraDeleteMultipleRequest;
import com.example.camerasurveillancesystem.dto.request.CameraStreamCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraStreamUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.CameraStreamResponse;
import com.example.camerasurveillancesystem.service.CameraStreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camera-streams")
@RequiredArgsConstructor
@Slf4j
public class CameraStreamController {

    private final CameraStreamService streamService;

    @PostMapping
    public ResponseEntity<ApiResponse<CameraStreamResponse>> createStream(
            @Valid @RequestBody CameraStreamCreateRequest request) {
        log.info("Request to create camera stream for camera ID: {}", request.getCameraId());
        
        CameraStreamResponse response = streamService.createStream(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo stream thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraStreamResponse>> updateStream(
            @PathVariable Long id,
            @Valid @RequestBody CameraStreamUpdateRequest request) {
        log.info("Request to update camera stream ID: {}", id);
        
        CameraStreamResponse response = streamService.updateStream(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật stream thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraStreamResponse>> getStreamById(@PathVariable Long id) {
        log.info("Request to get camera stream by ID: {}", id);
        
        CameraStreamResponse response = streamService.getStreamById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CameraStreamResponse>>> getAllStreams() {
        log.info("Request to get all camera streams");
        
        List<CameraStreamResponse> streams = streamService.getAllStreams();
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<List<CameraStreamResponse>>> getStreamsByCameraId(
            @PathVariable Long cameraId) {
        log.info("Request to get streams for camera ID: {}", cameraId);
        
        List<CameraStreamResponse> streams = streamService.getStreamsByCameraId(cameraId);
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @GetMapping("/type/{streamType}")
    public ResponseEntity<ApiResponse<List<CameraStreamResponse>>> getStreamsByType(
            @PathVariable String streamType) {
        log.info("Request to get streams by type: {}", streamType);
        
        List<CameraStreamResponse> streams = streamService.getStreamsByType(streamType);
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CameraStreamResponse>>> getActiveStreams() {
        log.info("Request to get all active streams");
        
        List<CameraStreamResponse> streams = streamService.getActiveStreams();
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStream(@PathVariable Long id) {
        log.info("Request to delete camera stream ID: {}", id);
        
        streamService.deleteStream(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa stream thành công", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleStreams(
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to delete multiple camera streams: {} streams", request.getIds().size());
        
        streamService.deleteMultipleStreams(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thành công " + request.getIds().size() + " stream", null));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CameraStreamResponse>> toggleStreamStatus(@PathVariable Long id) {
        log.info("Request to toggle stream status for ID: {}", id);
        
        CameraStreamResponse response = streamService.toggleStreamStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Thay đổi trạng thái stream thành công", response));
    }
}
