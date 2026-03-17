package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    
    /**
     * Get logs by user ID with pagination
     */
    Page<UserActivityLog> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Get logs by action type
     */
    Page<UserActivityLog> findByAction(String action, Pageable pageable);
    
    /**
     * Get logs by resource type
     */
    Page<UserActivityLog> findByResourceType(String resourceType, Pageable pageable);
    
    /**
     * Get logs by user and action
     */
    Page<UserActivityLog> findByUserIdAndAction(Long userId, String action, Pageable pageable);
    
    /**
     * Get logs within date range
     */
    Page<UserActivityLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Get recent logs
     */
    List<UserActivityLog> findTop100ByOrderByCreatedAtDesc();
    
    /**
     * Get logs by status
     */
    Page<UserActivityLog> findByStatus(String status, Pageable pageable);
    
    /**
     * Get failed operations
     */
    @Query("SELECT l FROM UserActivityLog l WHERE l.status = 'FAILED' ORDER BY l.createdAt DESC")
    List<UserActivityLog> findRecentFailedOperations(Pageable pageable);
    
    /**
     * Count logs by user
     */
    long countByUserId(Long userId);
    
    /**
     * Count logs by action
     */
    long countByAction(String action);
}
