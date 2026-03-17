package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.alert.AlertLogCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertLogResponse;
import com.example.camerasurveillancesystem.service.AlertLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alert-logs")
@RequiredArgsConstructor
public class AlertLogController {

    private final AlertLogService alertLogService;

    /**
     * Tạo alert log mới (ghi log thao tác)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AlertLogResponse>> createAlertLog(
            @Valid @RequestBody AlertLogCreateRequest request) {
        AlertLogResponse log = alertLogService.createLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alert log created successfully", log));
    }

    /**
     * Get logs theo alert
     */
    @GetMapping("/alert/{alertId}")
    public ResponseEntity<ApiResponse<List<AlertLogResponse>>> getLogsByAlert(
            @PathVariable Long alertId) {
        List<AlertLogResponse> logs = alertLogService.getLogsByAlertId(alertId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Get logs theo user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<AlertLogResponse>>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<AlertLogResponse> logs = alertLogService.getLogsByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Get recent logs
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AlertLogResponse>>> getRecentLogs(
            @RequestParam(defaultValue = "50") int limit) {
        List<AlertLogResponse> logs = alertLogService.getRecentLogs(limit);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
