package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.CameraCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraDeleteMultipleRequest;
import com.example.camerasurveillancesystem.dto.request.CameraSearchRequest;
import com.example.camerasurveillancesystem.dto.request.CameraUpdateRequest;
import com.example.camerasurveillancesystem.dto.request.RtspTestRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.CameraGroupResponse;
import com.example.camerasurveillancesystem.dto.response.CameraResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.RtspTestResponse;
import com.example.camerasurveillancesystem.service.CameraGroupService;
import com.example.camerasurveillancesystem.service.CameraService;
import com.example.camerasurveillancesystem.service.RtspConnectionTestResult;
import com.example.camerasurveillancesystem.service.VideoStreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cameras")
@RequiredArgsConstructor
@Slf4j
public class CameraController {

    private final CameraService cameraService;
    private final CameraGroupService cameraGroupService;
    private final VideoStreamService videoStreamService;

    /**
     * Tạo camera mới
     * POST /api/cameras
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CameraResponse>> createCamera(
            @Valid @RequestBody CameraCreateRequest request) {
        log.info("Request to create camera: {}", request.getCode());
        
        CameraResponse response = cameraService.createCamera(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo camera thành công", response));
    }

    /**
     * Cập nhật camera
     * PUT /api/cameras/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraResponse>> updateCamera(
            @PathVariable Long id,
            @Valid @RequestBody CameraUpdateRequest request) {
        log.info("Request to update camera ID: {}", id);
        
        CameraResponse response = cameraService.updateCamera(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật camera thành công", response));
    }

    /**
     * Lấy chi tiết camera theo ID
     * GET /api/cameras/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraResponse>> getCameraById(@PathVariable Long id) {
        log.info("Request to get camera by ID: {}", id);
        
        CameraResponse response = cameraService.getCameraById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy camera theo code
     * GET /api/cameras/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CameraResponse>> getCameraByCode(@PathVariable String code) {
        log.info("Request to get camera by code: {}", code);
        
        CameraResponse response = cameraService.getCameraByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy tất cả camera
     * GET /api/cameras
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CameraResponse>>> getAllCameras() {
        log.info("Request to get all cameras");
        
        List<CameraResponse> cameras = cameraService.getAllCameras();
        return ResponseEntity.ok(ApiResponse.success(cameras));
    }

    /**
     * Tìm kiếm camera với filter và phân trang
     * POST /api/cameras/search
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CameraResponse>>> searchCameras(
            @RequestBody CameraSearchRequest request) {
        log.info("Request to search cameras with filters");
        
        PageResponse<CameraResponse> response = cameraService.searchCameras(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Xóa camera theo ID
     * DELETE /api/cameras/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCamera(@PathVariable Long id) {
        log.info("Request to delete camera ID: {}", id);
        
        cameraService.deleteCamera(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa camera thành công", null));
    }

    /**
     * Xóa nhiều camera
     * DELETE /api/cameras/batch
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleCameras(
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to delete multiple cameras: {} cameras", request.getIds().size());
        
        cameraService.deleteMultipleCameras(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thành công " + request.getIds().size() + " camera", null));
    }

    /**
     * Kiểm tra camera tồn tại theo code
     * GET /api/cameras/exists/{code}
     */
    @GetMapping("/exists/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkCameraExists(@PathVariable String code) {
        log.info("Request to check if camera exists with code: {}", code);
        
        boolean exists = cameraService.existsByCode(code);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * Đếm số camera theo trạng thái
     * GET /api/cameras/count/{status}
     */
    @GetMapping("/count/{status}")
    public ResponseEntity<ApiResponse<Long>> countCamerasByStatus(@PathVariable String status) {
        log.info("Request to count cameras by status: {}", status);
        
        long count = cameraService.countByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Cập nhật trạng thái camera
     * PATCH /api/cameras/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CameraResponse>> updateCameraStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Request to update camera status ID: {} to status: {}", id, status);
        
        CameraResponse response = cameraService.updateCameraStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", response));
    }

    /**
     * Lấy tất cả camera groups
     * GET /api/v1/cameras/groups
     */
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<CameraGroupResponse>>> getAllCameraGroups() {
        log.info("Request to get all camera groups via cameras endpoint");
        
        List<CameraGroupResponse> groups = cameraGroupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * Kiểm tra kết nối RTSP trước khi lưu camera.
     * POST /api/v1/cameras/test-rtsp
     */
    @PostMapping("/test-rtsp")
    public ResponseEntity<ApiResponse<RtspTestResponse>> testRtspConnection(
            @Valid @RequestBody RtspTestRequest request) {
        log.info("Request to test RTSP connection: {}", request.getRtspUrl());

        long startTime = System.currentTimeMillis();
        int timeout = request.getTimeoutSeconds() > 0 ? request.getTimeoutSeconds() : 5;
        RtspConnectionTestResult testResult = videoStreamService.testRtspConnectionDetailed(request.getRtspUrl(), timeout);
        boolean success = testResult.isSuccess();
        long elapsed = System.currentTimeMillis() - startTime;

        String resultMessage = success
            ? "Kết nối RTSP thành công (" + elapsed + "ms)"
            : testResult.getDiagnosticMessage();

        RtspTestResponse response = RtspTestResponse.builder()
                .success(success)
                .message(success
                        ? "Kết nối RTSP thành công (" + elapsed + "ms)"
                : resultMessage)
            .diagnosticCode(testResult.getDiagnosticCode())
            .rtspUrl(request.getRtspUrl())
            .normalizedRtspUrl(testResult.getNormalizedRtspUrl())
            .responseTimeMs(elapsed)
                .build();

        ApiResponse<RtspTestResponse> apiResponse = ApiResponse.<RtspTestResponse>builder()
            .success(success)
            .message(resultMessage)
            .data(response)
            .build();

        return ResponseEntity.status(success ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
            .body(apiResponse);
    }
}
