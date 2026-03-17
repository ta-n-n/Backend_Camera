package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get overall system statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatistics() {
        Map<String, Object> stats = dashboardService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get camera statistics
     */
    @GetMapping("/statistics/cameras")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCameraStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        
        Map<String, Object> stats = dashboardService.getCameraStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get alert statistics
     */
    @GetMapping("/statistics/alerts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        
        Map<String, Object> stats = dashboardService.getAlertStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get AI detection statistics
     */
    @GetMapping("/statistics/ai-detections")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiDetectionStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        
        Map<String, Object> stats = dashboardService.getAiDetectionStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get storage statistics
     */
    @GetMapping("/statistics/storage")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStorageStatistics() {
        Map<String, Object> stats = dashboardService.getStorageStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get performance metrics
     */
    @GetMapping("/metrics/performance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceMetrics() {
        Map<String, Object> metrics = dashboardService.getPerformanceMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /**
     * Get activity timeline
     */
    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActivityTimeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        if (startDate == null) startDate = LocalDateTime.now().minusDays(7);
        if (endDate == null) endDate = LocalDateTime.now();
        
        Map<String, Object> timeline = dashboardService.getActivityTimeline(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }
}
