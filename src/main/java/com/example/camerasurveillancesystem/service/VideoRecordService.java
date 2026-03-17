package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.media.VideoRecordCreateRequest;
import com.example.camerasurveillancesystem.dto.request.media.VideoRecordSearchRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.media.StorageStatistics;
import com.example.camerasurveillancesystem.dto.response.media.VideoRecordResponse;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoRecordService {

    /**
     * Tạo video record mới
     */
    VideoRecordResponse createVideoRecord(VideoRecordCreateRequest request);

    /**
     * Get video by ID
     */
    VideoRecordResponse getVideoById(Long id);

    /**
     * Search videos
     */
    PageResponse<VideoRecordResponse> searchVideos(VideoRecordSearchRequest request);

    /**
     * Get videos by camera
     */
    PageResponse<VideoRecordResponse> getVideosByCamera(Long cameraId, Integer page, Integer size);

    /**
     * Get videos by date range
     */
    PageResponse<VideoRecordResponse> getVideosByDateRange(LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size);

    /**
     * Get videos by event
     */
    List<VideoRecordResponse> getVideosByEventId(Long eventId);

    /**
     * Stream video (for playback)
     */
    Resource streamVideo(Long id);

    /**
     * Download video
     */
    Resource downloadVideo(Long id);

    /**
     * Get storage statistics
     */
    StorageStatistics getStorageStatistics();

    /**
     * Cleanup old videos
     */
    int cleanupOldVideos(Integer daysToKeep);

    /**
     * Delete video
     */
    void deleteVideo(Long id);

    /**
     * Delete videos by camera
     */
    void deleteVideosByCamera(Long cameraId);
}
