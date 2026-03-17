package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

    /**
     * Tìm channel theo name
     */
    Optional<NotificationChannel> findByName(String name);

    /**
     * Kiểm tra name đã tồn tại
     */
    boolean existsByName(String name);

    /**
     * Tìm channels theo type
     */
    Page<NotificationChannel> findByChannelType(String channelType, Pageable pageable);

    /**
     * Tìm channels active
     */
    List<NotificationChannel> findByIsActiveTrue();

    /**
     * Tìm channels theo type và active
     */
    List<NotificationChannel> findByChannelTypeAndIsActiveTrue(String channelType);

    /**
     * Đếm channels theo type
     */
    long countByChannelType(String channelType);

    /**
     * Đếm active channels
     */
    long countByIsActiveTrue();

    /**
     * Tìm channel theo configuration (JSON search)
     */
    @Query("SELECT nc FROM NotificationChannel nc WHERE nc.configuration LIKE %:searchValue%")
    List<NotificationChannel> findByConfigurationContaining(@Param("searchValue") String searchValue);
}
