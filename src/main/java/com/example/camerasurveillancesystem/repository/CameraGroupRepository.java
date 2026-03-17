package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.CameraGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraGroupRepository extends JpaRepository<CameraGroup, Long> {
}
