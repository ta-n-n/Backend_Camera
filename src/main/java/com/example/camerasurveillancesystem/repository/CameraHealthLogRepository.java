package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.CameraHealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CameraHealthLogRepository extends JpaRepository<CameraHealthLog, Long>, JpaSpecificationExecutor<CameraHealthLog> {

    List<CameraHealthLog> findByCameraId(Long cameraId);

    List<CameraHealthLog> findByStatus(String status);

    List<CameraHealthLog> findByCameraIdAndStatus(Long cameraId, String status);

    @Query("SELECT h FROM CameraHealthLog h WHERE h.camera.id = :cameraId AND h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
    List<CameraHealthLog> findByCameraIdAndDateRange(
        @Param("cameraId") Long cameraId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT h FROM CameraHealthLog h WHERE h.camera.id = :cameraId ORDER BY h.checkedAt DESC")
    List<CameraHealthLog> findLatestByCameraId(@Param("cameraId") Long cameraId);

    List<CameraHealthLog> findByIdIn(List<Long> ids);

    void deleteByCameraId(Long cameraId);

    void deleteByCheckedAtBefore(LocalDateTime date);
}
