package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageCreateRequest;
import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageSearchRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.media.SnapshotImageResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface SnapshotImageService {

    /**
     * Tạo snapshot mới
     */
    SnapshotImageResponse createSnapshot(SnapshotImageCreateRequest request);

    /**
     * Capture snapshot từ camera (manual)
     */
    SnapshotImageResponse captureSnapshot(Long cameraId, String description);

    /**
     * Upload snapshot image
     */
    SnapshotImageResponse uploadSnapshot(Long cameraId, MultipartFile file, String description, String triggerType);

    /**
     * Get snapshot by ID
     */
    SnapshotImageResponse getSnapshotById(Long id);

    /**
     * Search snapshots
     */
    PageResponse<SnapshotImageResponse> searchSnapshots(SnapshotImageSearchRequest request);

    /**
     * Get snapshots by camera
     */
    PageResponse<SnapshotImageResponse> getSnapshotsByCamera(Long cameraId, Integer page, Integer size);

    /**
     * Get snapshots by date range
     */
    PageResponse<SnapshotImageResponse> getSnapshotsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size);

    /**
     * Get snapshots by event
     */
    List<SnapshotImageResponse> getSnapshotsByEventId(Long eventId);

    /**
     * Get snapshots by alert
     */
    List<SnapshotImageResponse> getSnapshotsByAlertId(Long alertId);

    /**
     * Get latest snapshot of camera
     */
    SnapshotImageResponse getLatestSnapshot(Long cameraId);

    /**
     * View image
     */
    Resource viewImage(Long id);

    /**
     * Download image
     */
    Resource downloadImage(Long id);

    /**
     * Generate thumbnail
     */
    Resource getThumbnail(Long id, Integer width, Integer height);

    /**
     * Cleanup old snapshots
     */
    int cleanupOldSnapshots(Integer daysToKeep);

    /**
     * Delete snapshot
     */
    void deleteSnapshot(Long id);

    /**
     * Delete snapshots by camera
     */
    void deleteSnapshotsByCamera(Long cameraId);
}
