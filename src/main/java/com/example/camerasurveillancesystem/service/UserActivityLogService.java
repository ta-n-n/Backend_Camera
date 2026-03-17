package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.UserActivityLogResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityLogService {
    
    /**
     * Log user activity
     */
    void logActivity(String username, String action, String resourceType, Long resourceId, 
                    String description, HttpServletRequest request);
    
    /**
     * Log activity with status and execution time
     */
    void logActivity(String username, String action, String resourceType, Long resourceId,
                    String description, String status, Long executionTime, HttpServletRequest request);
    
    /**
     * Log activity with old and new values (for audit trail)
     */
    void logActivity(String username, String action, String resourceType, Long resourceId,
                    String description, String oldValue, String newValue, HttpServletRequest request);
    
    /**
     * Get all logs with pagination
     */
    PageResponse<UserActivityLogResponse> getAllLogs(Pageable pageable);
    
    /**
     * Get logs by user
     */
    PageResponse<UserActivityLogResponse> getLogsByUser(Long userId, Pageable pageable);
    
    /**
     * Get logs by action
     */
    PageResponse<UserActivityLogResponse> getLogsByAction(String action, Pageable pageable);
    
    /**
     * Get logs by resource type
     */
    PageResponse<UserActivityLogResponse> getLogsByResourceType(String resourceType, Pageable pageable);
    
    /**
     * Get logs by date range
     */
    PageResponse<UserActivityLogResponse> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Get recent logs (last 100)
     */
    List<UserActivityLogResponse> getRecentLogs();
    
    /**
     * Get failed operations
     */
    List<UserActivityLogResponse> getFailedOperations(int limit);
    
    /**
     * Get activity count by user
     */
    long countActivitiesByUser(Long userId);
}
