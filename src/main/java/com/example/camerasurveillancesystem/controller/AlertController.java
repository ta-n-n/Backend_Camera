package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.alert.AlertCreateRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertDeleteMultipleRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertSearchRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertStatistics;
import com.example.camerasurveillancesystem.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * Tạo alert mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AlertResponse>> createAlert(
            @Valid @RequestBody AlertCreateRequest request) {
        AlertResponse alert = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alert created successfully", alert));
    }

    /**
     * Cập nhật alert
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponse>> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertUpdateRequest request) {
        AlertResponse alert = alertService.updateAlert(id, request);
        return ResponseEntity.ok(ApiResponse.success("Alert updated successfully", alert));
    }

    /**
     * Acknowledge alert (xác nhận đã nhận)
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponse>> acknowledgeAlert(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        AlertResponse alert = alertService.acknowledgeAlert(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged successfully", alert));
    }

    /**
     * Assign alert to user
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<AlertResponse>> assignAlert(
            @PathVariable Long id,
            @RequestParam Long userId) {
        AlertResponse alert = alertService.assignAlert(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert assigned successfully", alert));
    }

    /**
     * Resolve alert (đánh dấu đã xử lý)
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(
            @PathVariable Long id,
            @RequestParam(required = false) String resolutionNotes) {
        AlertResponse alert = alertService.resolveAlert(id, resolutionNotes);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", alert));
    }

    /**
     * Đánh dấu là false positive
     */
    @PostMapping("/{id}/false-positive")
    public ResponseEntity<ApiResponse<AlertResponse>> markAsFalsePositive(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        AlertResponse alert = alertService.markAsFalsePositive(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Alert marked as false positive", alert));
    }

    /**
     * Get alert by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(@PathVariable Long id) {
        AlertResponse alert = alertService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.success(alert));
    }

    /**
     * Search alerts với filters
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AlertResponse>>> searchAlerts(
            @ModelAttribute AlertSearchRequest request) {
        
        PageResponse<AlertResponse> alerts = alertService.searchAlerts(request);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * Get alerts by camera
     */
    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<PageResponse<AlertResponse>>> getAlertsByCamera(
            @PathVariable Long cameraId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<AlertResponse> alerts = alertService.getAlertsByCamera(cameraId, page, size);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * Get unresolved alerts (NEW hoặc ACKNOWLEDGED)
     */
    @GetMapping("/unresolved")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnresolvedAlerts(
            @RequestParam(defaultValue = "50") int limit) {
        List<AlertResponse> alerts = alertService.getUnresolvedAlerts(limit);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * Get critical unresolved alerts
     */
    @GetMapping("/critical")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getCriticalAlerts() {
        List<AlertResponse> alerts = alertService.getCriticalUnresolvedAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * Get alerts statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AlertStatistics>> getAlertStatistics() {
        AlertStatistics statistics = alertService.getAlertStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * Delete multiple alerts
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleAlerts(
            @Valid @RequestBody AlertDeleteMultipleRequest request) {
        alertService.deleteMultipleAlerts(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Deleted " + request.getIds().size() + " alerts successfully", null));
    }

    /**
     * Delete alert
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok(ApiResponse.success("Alert deleted successfully", null));
    }
}
