package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.SnapshotImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SnapshotImageRepository extends JpaRepository<SnapshotImage, Long>, JpaSpecificationExecutor<SnapshotImage> {

    /**
     * Tìm snapshots theo camera
     */
    Page<SnapshotImage> findByCameraId(Long cameraId, Pageable pageable);

    /**
     * Tìm snapshots theo khoảng thời gian
     */
    @Query("SELECT s FROM SnapshotImage s WHERE s.capturedAt BETWEEN :startDate AND :endDate ORDER BY s.capturedAt DESC")
    Page<SnapshotImage> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * Tìm snapshots theo camera và thời gian
     */
    @Query("SELECT s FROM SnapshotImage s WHERE s.camera.id = :cameraId " +
           "AND s.capturedAt BETWEEN :startDate AND :endDate ORDER BY s.capturedAt DESC")
    Page<SnapshotImage> findByCameraAndDateRange(@Param("cameraId") Long cameraId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 Pageable pageable);

    /**
     * Tìm snapshots theo trigger type
     */
    Page<SnapshotImage> findByCaptureTypeOrderByCapturedAtDesc(String captureType, Pageable pageable);

    /**
     * Tìm snapshot gần nhất của camera
     */
    @Query("SELECT s FROM SnapshotImage s WHERE s.camera.id = :cameraId ORDER BY s.capturedAt DESC LIMIT 1")
    Optional<SnapshotImage> findLatestByCameraId(@Param("cameraId") Long cameraId);

    /**
     * Tìm snapshots theo AI event (nếu có liên kết)
     */
    @Query("SELECT s FROM SnapshotImage s WHERE s.aiEventId = :eventId ORDER BY s.capturedAt DESC")
    List<SnapshotImage> findByAiEventId(@Param("eventId") Long eventId);

    /**
     * Tìm snapshots theo alert (nếu có liên kết)
     */
    @Query("SELECT s FROM SnapshotImage s WHERE s.alertId = :alertId ORDER BY s.capturedAt DESC")
    List<SnapshotImage> findByAlertId(@Param("alertId") Long alertId);

    /**
     * Tổng dung lượng snapshots
     */
    @Query("SELECT SUM(s.fileSize) FROM SnapshotImage s")
    Long getTotalStorageSize();

    /**
     * Tổng dung lượng theo camera
     */
    @Query("SELECT SUM(s.fileSize) FROM SnapshotImage s WHERE s.camera.id = :cameraId")
    Long getStorageSizeByCameraId(@Param("cameraId") Long cameraId);

    /**
     * Đếm snapshots theo camera
     */
    long countByCameraId(Long cameraId);

    /**
     * Đếm snapshots theo capture type
     */
    long countByCaptureType(String captureType);

    /**
     * Tìm snapshots cũ cần xóa
     */
    @Query("SELECT s FROM SnapshotImage s WHERE s.capturedAt < :threshold ORDER BY s.capturedAt ASC")
    List<SnapshotImage> findOldSnapshotsForCleanup(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    /**
     * Kiểm tra file path đã tồn tại
     */
    boolean existsByFilePath(String filePath);

    /**
     * Xóa snapshots cũ theo camera
     */
    @Query("DELETE FROM SnapshotImage s WHERE s.camera.id = :cameraId AND s.capturedAt < :threshold")
    void deleteOldSnapshotsByCameraId(@Param("cameraId") Long cameraId, @Param("threshold") LocalDateTime threshold);
}
