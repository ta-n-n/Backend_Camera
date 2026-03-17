package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    Page<SystemLog> findByLevel(String level, Pageable pageable);

    Page<SystemLog> findByModule(String module, Pageable pageable);

    @Query("SELECT sl FROM SystemLog sl WHERE sl.createdAt BETWEEN :start AND :end ORDER BY sl.createdAt DESC")
    Page<SystemLog> findByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("SELECT sl FROM SystemLog sl WHERE sl.level = 'ERROR' OR sl.level = 'CRITICAL' ORDER BY sl.createdAt DESC")
    Page<SystemLog> findRecentErrors(Pageable pageable);

    @Query("SELECT sl FROM SystemLog sl WHERE sl.module = :module AND sl.createdAt >= :since ORDER BY sl.createdAt DESC")
    List<SystemLog> findRecentByModule(@Param("module") String module, @Param("since") LocalDateTime since);

    long countByLevelAndCreatedAtAfter(String level, LocalDateTime after);

    @Query("SELECT sl.level, COUNT(sl) FROM SystemLog sl WHERE sl.createdAt >= :since GROUP BY sl.level")
    List<Object[]> countByLevelGrouped(@Param("since") LocalDateTime since);

    @Query("SELECT sl.module, COUNT(sl) FROM SystemLog sl WHERE sl.createdAt >= :since GROUP BY sl.module")
    List<Object[]> countByModuleGrouped(@Param("since") LocalDateTime since);

    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
