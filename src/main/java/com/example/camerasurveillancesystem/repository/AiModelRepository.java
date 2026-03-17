package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiModelRepository extends JpaRepository<AiModel, Long> {

    List<AiModel> findByIsActive(Boolean isActive);

    List<AiModel> findByType(String type);

    Optional<AiModel> findByNameAndVersion(String name, String version);

    boolean existsByNameAndVersion(String name, String version);

    @Query("SELECT m FROM AiModel m WHERE m.isActive = true AND m.type = :type")
    List<AiModel> findActiveModelsByType(String type);
}
