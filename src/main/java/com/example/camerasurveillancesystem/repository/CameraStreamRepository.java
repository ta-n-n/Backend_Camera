package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.CameraStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraStreamRepository extends JpaRepository<CameraStream, Long> {

    List<CameraStream> findByCameraId(Long cameraId);

    List<CameraStream> findByStreamType(String streamType);

    List<CameraStream> findByIsActive(Boolean isActive);

    List<CameraStream> findByCameraIdAndIsActive(Long cameraId, Boolean isActive);

    List<CameraStream> findByIdIn(List<Long> ids);
}
