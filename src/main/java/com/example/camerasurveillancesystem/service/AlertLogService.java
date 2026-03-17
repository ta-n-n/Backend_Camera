package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.alert.AlertLogCreateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertLogResponse;

import java.util.List;

public interface AlertLogService {

    /**
     * Tạo log mới
     */
    AlertLogResponse createLog(AlertLogCreateRequest request);

    /**
     * Get logs by alert
     */
    List<AlertLogResponse> getLogsByAlertId(Long alertId);

    /**
     * Get logs by user
     */
    PageResponse<AlertLogResponse> getLogsByUserId(Long userId, Integer page, Integer size);

    /**
     * Get recent logs
     */
    List<AlertLogResponse> getRecentLogs(Integer limit);
}
