package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.AlertLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {

    /**
     * Tìm logs theo alert
     */
    Page<AlertLog> findByAlertIdOrderByCreatedAtDesc(Long alertId, Pageable pageable);

    /**
     * Tìm logs theo alert (list)
     */
    List<AlertLog> findByAlertIdOrderByCreatedAtDesc(Long alertId);

    /**
     * Tìm logs theo user
     */
    Page<AlertLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Tìm logs theo action type
     */
    Page<AlertLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /**
     * Tìm logs trong khoảng thời gian
     */
    @Query("SELECT al FROM AlertLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AlertLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    /**
     * Tìm log gần nhất của alert
     */
    @Query("SELECT al FROM AlertLog al WHERE al.alert.id = :alertId ORDER BY al.createdAt DESC LIMIT 1")
    AlertLog findLatestLogByAlertId(@Param("alertId") Long alertId);

    /**
     * Đếm logs theo action
     */
    long countByAction(String action);

    /**
     * Tìm alerts đã được resolved bởi user
     */
    @Query("SELECT al FROM AlertLog al WHERE al.action = 'RESOLVED' AND al.user.id = :userId " +
           "ORDER BY al.createdAt DESC")
    Page<AlertLog> findResolvedAlertsByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Đếm số lần user xử lý alerts
     */
    @Query("SELECT COUNT(al) FROM AlertLog al WHERE al.user.id = :userId AND al.action = 'RESOLVED'")
    long countResolvedAlertsByUser(@Param("userId") Long userId);

    /**
     * Tìm comments của alert
     */
    @Query("SELECT al FROM AlertLog al WHERE al.alert.id = :alertId AND al.action = 'COMMENTED' " +
           "ORDER BY al.createdAt DESC")
    List<AlertLog> findCommentsByAlertId(@Param("alertId") Long alertId);
}
