package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.media.VideoRecordCreateRequest;
import com.example.camerasurveillancesystem.dto.request.media.VideoRecordSearchRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.media.VideoRecordResponse;
import com.example.camerasurveillancesystem.service.VideoRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoRecordController {

    private final VideoRecordService videoService;

    /**
     * Tạo video record mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VideoRecordResponse>> createVideoRecord(
            @Valid @RequestBody VideoRecordCreateRequest request) {
        VideoRecordResponse video = videoService.createVideoRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Video record created successfully", video));
    }

    /**
     * Get video by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoRecordResponse>> getVideoById(@PathVariable Long id) {
        VideoRecordResponse video = videoService.getVideoById(id);
        return ResponseEntity.ok(ApiResponse.success(video));
    }

    /**
     * Search videos
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<VideoRecordResponse>>> searchVideos(
            @ModelAttribute VideoRecordSearchRequest request) {
        PageResponse<VideoRecordResponse> videos = videoService.searchVideos(request);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }

    /**
     * Get videos by camera
     */
    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<PageResponse<VideoRecordResponse>>> getVideosByCamera(
            @PathVariable Long cameraId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<VideoRecordResponse> videos = videoService.getVideosByCamera(cameraId, page, size);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }

    /**
     * Stream video (HTTP range requests support)
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long id) {
        Resource video = videoService.streamVideo(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(video);
    }

    /**
     * Download video
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadVideo(@PathVariable Long id) {
        VideoRecordResponse videoInfo = videoService.getVideoById(id);
        Resource video = videoService.downloadVideo(id);
        
        String filename = "video_" + id + ".mp4";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"")
                .body(video);
    }

    /**
     * Get storage statistics
     */
    @GetMapping("/storage/statistics")
    public ResponseEntity<ApiResponse<com.example.camerasurveillancesystem.dto.response.media.StorageStatistics>> getStorageStatistics() {
        com.example.camerasurveillancesystem.dto.response.media.StorageStatistics stats = videoService.getStorageStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Delete video
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.success("Video deleted successfully", null));
    }

    /**
     * Cleanup old videos
     */
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldVideos(
            @RequestParam(defaultValue = "30") int retentionDays) {
        int deletedCount = videoService.cleanupOldVideos(retentionDays);
        return ResponseEntity.ok(ApiResponse.success("Cleanup completed", deletedCount));
    }
}
