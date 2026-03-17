package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.AiEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiEventRepository extends JpaRepository<AiEvent, Long>, JpaSpecificationExecutor<AiEvent> {

    List<AiEvent> findByCameraId(Long cameraId);

    List<AiEvent> findByEventType(String eventType);

    List<AiEvent> findByModelId(Long modelId);

    @Query("SELECT e FROM AiEvent e WHERE e.camera.id = :cameraId AND e.detectedAt BETWEEN :startDate AND :endDate")
    List<AiEvent> findByCameraIdAndDateRange(
        @Param("cameraId") Long cameraId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM AiEvent e WHERE e.eventType = :eventType AND e.confidenceScore >= :minConfidence")
    List<AiEvent> findByEventTypeAndMinConfidence(
        @Param("eventType") String eventType,
        @Param("minConfidence") Double minConfidence
    );

    List<AiEvent> findByIdIn(List<Long> ids);

    long countByEventType(String eventType);

    @Query("SELECT e FROM AiEvent e WHERE e.detectedAt >= :since ORDER BY e.detectedAt DESC")
    List<AiEvent> findRecentEvents(@Param("since") LocalDateTime since);
}
