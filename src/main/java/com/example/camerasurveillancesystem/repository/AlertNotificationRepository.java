package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.AlertNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertNotificationRepository extends JpaRepository<AlertNotification, Long> {

    /**
     * Tìm notifications theo alert
     */
    Page<AlertNotification> findByAlertIdOrderBySentAtDesc(Long alertId, Pageable pageable);

    /**
     * Tìm notifications theo channel
     */
    Page<AlertNotification> findByChannelIdOrderBySentAtDesc(Long channelId, Pageable pageable);

    /**
     * Tìm notifications theo status
     */
    Page<AlertNotification> findByStatusOrderBySentAtDesc(String status, Pageable pageable);

    /**
     * Tìm notifications failed (cần retry)
     */
    @Query("SELECT an FROM AlertNotification an WHERE an.status = 'FAILED' AND an.retryCount < :maxRetries " +
           "ORDER BY an.sentAt ASC")
    List<AlertNotification> findFailedNotificationsForRetry(@Param("maxRetries") Integer maxRetries);

    /**
     * Tìm notifications theo recipient address
     */
    Page<AlertNotification> findByRecipientAddressContainingOrderBySentAtDesc(String recipientAddress, Pageable pageable);

    /**
     * Đếm notifications theo status
     */
    long countByStatus(String status);

    /**
     * Đếm notifications sent trong khoảng thời gian
     */
    @Query("SELECT COUNT(an) FROM AlertNotification an WHERE an.sentAt BETWEEN :startDate AND :endDate " +
           "AND an.status = 'SENT'")
    long countSentNotificationsInDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm notifications gần đây của alert
     */
    @Query("SELECT an FROM AlertNotification an WHERE an.alert.id = :alertId ORDER BY an.sentAt DESC LIMIT 5")
    List<AlertNotification> findRecentNotificationsByAlertId(@Param("alertId") Long alertId);

    /**
     * Kiểm tra đã gửi notification cho alert qua channel chưa
     */
    @Query("SELECT COUNT(an) > 0 FROM AlertNotification an WHERE an.alert.id = :alertId " +
           "AND an.channel.id = :channelId AND an.status = 'SENT'")
    boolean hasNotificationBeenSent(@Param("alertId") Long alertId, @Param("channelId") Long channelId);

    /**
     * Tìm notifications theo khoảng thời gian
     */
    @Query("SELECT an FROM AlertNotification an WHERE an.sentAt BETWEEN :startDate AND :endDate " +
           "ORDER BY an.sentAt DESC")
    Page<AlertNotification> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    /**
     * Xóa notifications cũ
     */
    @Query("DELETE FROM AlertNotification an WHERE an.sentAt < :threshold")
    void deleteOldNotifications(@Param("threshold") LocalDateTime threshold);
}
