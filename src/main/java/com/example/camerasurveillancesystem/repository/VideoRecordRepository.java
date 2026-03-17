package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.VideoRecord;
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
public interface VideoRecordRepository extends JpaRepository<VideoRecord, Long>, JpaSpecificationExecutor<VideoRecord> {

    /**
     * Tìm videos theo camera (thông qua stream)
     */
    @Query("SELECT v FROM VideoRecord v WHERE v.stream.camera.id = :cameraId")
    Page<VideoRecord> findByCameraId(@Param("cameraId") Long cameraId, Pageable pageable);

    /**
     * Tìm videos theo stream
     */
    Page<VideoRecord> findByStreamId(Long streamId, Pageable pageable);

    /**
     * Tìm videos theo khoảng thời gian
     */
    @Query("SELECT v FROM VideoRecord v WHERE v.startTime BETWEEN :startDate AND :endDate ORDER BY v.startTime DESC")
    Page<VideoRecord> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    /**
     * Tìm videos theo camera và thời gian
     */
    @Query("SELECT v FROM VideoRecord v WHERE v.stream.camera.id = :cameraId " +
           "AND v.startTime BETWEEN :startDate AND :endDate ORDER BY v.startTime DESC")
    Page<VideoRecord> findByCameraAndDateRange(@Param("cameraId") Long cameraId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    /**
     * Tìm videos theo AI event
     */
    List<VideoRecord> findByAiEventId(Long aiEventId);

    /**
     * Tổng dung lượng videos
     */
    @Query("SELECT SUM(v.fileSize) FROM VideoRecord v")
    Long getTotalStorageSize();

    /**
     * Tổng dung lượng theo camera
     */
    @Query("SELECT SUM(v.fileSize) FROM VideoRecord v WHERE v.stream.camera.id = :cameraId")
    Long getStorageSizeByCameraId(@Param("cameraId") Long cameraId);

    /**
     * Đếm videos theo camera
     */
    @Query("SELECT COUNT(v) FROM VideoRecord v WHERE v.stream.camera.id = :cameraId")
    long countByCameraId(@Param("cameraId") Long cameraId);

    /**
     * Tìm videos cũ cần xóa (cleanup)
     */
    @Query("SELECT v FROM VideoRecord v WHERE v.createdAt < :threshold ORDER BY v.createdAt ASC")
    List<VideoRecord> findOldVideosForCleanup(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    /**
     * Tìm videos lớn nhất
     */
    @Query("SELECT v FROM VideoRecord v ORDER BY v.fileSize DESC")
    List<VideoRecord> findLargestVideos(Pageable pageable);

    /**
     * Tìm videos theo duration
     */
    @Query("SELECT v FROM VideoRecord v WHERE v.duration >= :minDuration ORDER BY v.duration DESC")
    Page<VideoRecord> findByMinDuration(@Param("minDuration") Integer minDuration, Pageable pageable);

    /**
     * Kiểm tra file path đã tồn tại
     */
    boolean existsByFilePath(String filePath);

    /**
     * Xóa videos cũ theo camera
     */
    @Query("DELETE FROM VideoRecord v WHERE v.stream.camera.id = :cameraId AND v.createdAt < :threshold")
    void deleteOldVideosByCameraId(@Param("cameraId") Long cameraId, @Param("threshold") LocalDateTime threshold);
}
