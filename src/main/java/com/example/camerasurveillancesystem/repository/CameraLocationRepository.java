package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.CameraLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraLocationRepository extends JpaRepository<CameraLocation, Long> {
}
