package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system-logs")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * Get system logs with filters
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        PageResponse<Map<String, Object>> logs = systemLogService.getLogs(
                level, module, startDate, endDate, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Get recent errors
     */
    @GetMapping("/errors")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getRecentErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        PageResponse<Map<String, Object>> errors = systemLogService.getRecentErrors(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(errors));
    }

    /**
     * Get log statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(defaultValue = "24") int hours
    ) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        Map<String, Object> stats = systemLogService.getLogStatistics(since);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Delete old logs
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupOldLogs(
            @RequestParam(defaultValue = "90") int daysToKeep
    ) {
        systemLogService.deleteOldLogs(daysToKeep);
        return ResponseEntity.ok(ApiResponse.success("Old logs deleted successfully", null));
    }
}
