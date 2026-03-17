package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    /**
     * Tìm alerts theo camera (thông qua aiEvent)
     */
    @Query("SELECT a FROM Alert a WHERE a.aiEvent.camera.id = :cameraId")
    Page<Alert> findByCameraId(@Param("cameraId") Long cameraId, Pageable pageable);

    /**
     * Tìm alerts theo AI event
     */
    Page<Alert> findByAiEventId(Long aiEventId, Pageable pageable);

    /**
     * Tìm alerts theo type
     */
    Page<Alert> findByAlertType(String alertType, Pageable pageable);

    /**
     * Tìm alerts theo severity
     */
    Page<Alert> findBySeverity(String severity, Pageable pageable);

    /**
     * Tìm alerts theo status
     */
    Page<Alert> findByStatus(String status, Pageable pageable);

    /**
     * Tìm alerts chưa xử lý
     */
       @Query("SELECT a FROM Alert a WHERE a.status IN ('PENDING', 'NEW', 'ACKNOWLEDGED') ORDER BY a.severity DESC, a.createdAt DESC")
    List<Alert> findUnresolvedAlerts(Pageable pageable);

    /**
     * Tìm alerts theo khoảng thời gian
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<Alert> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);

    /**
     * Đếm alerts theo status
     */
    long countByStatus(String status);

    /**
     * Đếm alerts theo severity
     */
    long countBySeverity(String severity);

    /**
     * Tìm critical alerts chưa xử lý
     */
       @Query("SELECT a FROM Alert a WHERE a.severity = 'CRITICAL' AND a.status IN ('PENDING', 'NEW', 'ACKNOWLEDGED') ORDER BY a.createdAt DESC")
    List<Alert> findCriticalUnresolvedAlerts();

    /**
     * Tìm alerts cần escalate (quá thời gian không xử lý)
     */
       @Query("SELECT a FROM Alert a WHERE a.status IN ('PENDING', 'NEW') AND a.createdAt < :threshold")
    List<Alert> findAlertsNeedEscalation(@Param("threshold") LocalDateTime threshold);

    /**
     * Kiểm tra có alert trùng lặp không (thông qua aiEvent)
     */
    @Query("SELECT COUNT(a) > 0 FROM Alert a WHERE a.aiEvent.camera.id = :cameraId AND a.alertType = :alertType " +
           "AND a.status IN ('PENDING', 'NEW', 'ACKNOWLEDGED') AND a.createdAt > :since")
    boolean existsDuplicateAlert(@Param("cameraId") Long cameraId, 
                                 @Param("alertType") String alertType,
                                 @Param("since") LocalDateTime since);
}
