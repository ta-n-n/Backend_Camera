package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.UserActivityLogResponse;
import com.example.camerasurveillancesystem.service.UserActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activity-logs")
@RequiredArgsConstructor
@Tag(name = "User Activity Logs", description = "Quản lý log hoạt động người dùng (Audit Trail)")
public class UserActivityLogController {

    private final UserActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy tất cả activity logs (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityLogResponse>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<UserActivityLogResponse> response = activityLogService.getAllLogs(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    @Operation(summary = "Lấy activity logs của một user")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityLogResponse>>> getLogsByUser(
            @Parameter(description = "ID của user") @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<UserActivityLogResponse> response = activityLogService.getLogsByUser(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy logs theo loại hành động (LOGIN, CREATE, UPDATE, DELETE, etc.)")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityLogResponse>>> getLogsByAction(
            @Parameter(description = "Loại hành động") @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<UserActivityLogResponse> response = activityLogService.getLogsByAction(action, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/resource/{resourceType}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy logs theo loại resource (CAMERA, ALERT, USER, etc.)")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityLogResponse>>> getLogsByResourceType(
            @Parameter(description = "Loại resource") @PathVariable String resourceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<UserActivityLogResponse> response = activityLogService.getLogsByResourceType(resourceType, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy logs trong khoảng thời gian")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityLogResponse>>> getLogsByDateRange(
            @Parameter(description = "Ngày bắt đầu (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Ngày kết thúc (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<UserActivityLogResponse> response = activityLogService.getLogsByDateRange(startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy 100 logs gần nhất")
    public ResponseEntity<ApiResponse<List<UserActivityLogResponse>>> getRecentLogs() {
        List<UserActivityLogResponse> response = activityLogService.getRecentLogs();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/failed")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy các thao tác thất bại gần đây")
    public ResponseEntity<ApiResponse<List<UserActivityLogResponse>>> getFailedOperations(
            @RequestParam(defaultValue = "50") int limit) {
        List<UserActivityLogResponse> response = activityLogService.getFailedOperations(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    @Operation(summary = "Đếm số lượng hoạt động của user")
    public ResponseEntity<ApiResponse<Long>> countActivitiesByUser(
            @Parameter(description = "ID của user") @PathVariable Long userId) {
        long count = activityLogService.countActivitiesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
