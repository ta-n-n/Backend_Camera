package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.CameraHealthLogCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraHealthLogSearchRequest;
import com.example.camerasurveillancesystem.dto.response.CameraHealthLogResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface CameraHealthLogService {

    CameraHealthLogResponse createHealthLog(CameraHealthLogCreateRequest request);

    CameraHealthLogResponse getHealthLogById(Long id);

    List<CameraHealthLogResponse> getAllHealthLogs();

    List<CameraHealthLogResponse> getHealthLogsByCameraId(Long cameraId);

    List<CameraHealthLogResponse> getHealthLogsByStatus(String status);

    PageResponse<CameraHealthLogResponse> searchHealthLogs(CameraHealthLogSearchRequest request);

    void deleteHealthLog(Long id);

    void deleteMultipleHealthLogs(List<Long> ids);

    void deleteHealthLogsByCameraId(Long cameraId);

    void deleteOldHealthLogs(LocalDateTime beforeDate);

    CameraHealthLogResponse getLatestHealthLogByCameraId(Long cameraId);
}
