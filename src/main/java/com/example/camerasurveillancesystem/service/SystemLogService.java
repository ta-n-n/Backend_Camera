package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface SystemLogService {

    /**
     * Log information message
     */
    void logInfo(String module, String message, String source);

    /**
     * Log warning message
     */
    void logWarn(String module, String message, String source);

    /**
     * Log error message
     */
    void logError(String module, String message, String details, String source);

    /**
     * Log debug message
     */
    void logDebug(String module, String message, String source);

    /**
     * Get logs with filters
     */
    PageResponse<Map<String, Object>> getLogs(
            String level, 
            String module, 
            LocalDateTime start, 
            LocalDateTime end, 
            Pageable pageable
    );

    /**
     * Get recent errors
     */
    PageResponse<Map<String, Object>> getRecentErrors(Pageable pageable);

    /**
     * Get log statistics
     */
    Map<String, Object> getLogStatistics(LocalDateTime since);

    /**
     * Delete old logs
     */
    void deleteOldLogs(int daysToKeep);
}
