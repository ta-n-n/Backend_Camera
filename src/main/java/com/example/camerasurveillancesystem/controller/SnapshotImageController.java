package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageCreateRequest;
import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageSearchRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.media.SnapshotImageResponse;
import com.example.camerasurveillancesystem.service.SnapshotImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/snapshots")
@RequiredArgsConstructor
public class SnapshotImageController {

    private final SnapshotImageService snapshotService;

    /**
     * Capture snapshot từ camera
     */
    @PostMapping("/capture/camera/{cameraId}")
    public ResponseEntity<ApiResponse<SnapshotImageResponse>> captureSnapshot(
            @PathVariable Long cameraId,
            @RequestParam(required = false) String description) {
        SnapshotImageResponse snapshot = snapshotService.captureSnapshot(cameraId, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Snapshot captured successfully", snapshot));
    }

    /**
     * Upload snapshot image
     */
    @PostMapping("/upload/camera/{cameraId}")
    public ResponseEntity<ApiResponse<SnapshotImageResponse>> uploadSnapshot(
            @PathVariable Long cameraId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String triggerType) {
        SnapshotImageResponse snapshot = snapshotService.uploadSnapshot(cameraId, file, description, triggerType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Snapshot uploaded successfully", snapshot));
    }

    /**
     * Get snapshot by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SnapshotImageResponse>> getSnapshotById(@PathVariable Long id) {
        SnapshotImageResponse snapshot = snapshotService.getSnapshotById(id);
        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    /**
     * Search snapshots
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<SnapshotImageResponse>>> searchSnapshots(
            @ModelAttribute SnapshotImageSearchRequest request) {
        PageResponse<SnapshotImageResponse> snapshots = snapshotService.searchSnapshots(request);
        return ResponseEntity.ok(ApiResponse.success(snapshots));
    }

    /**
     * Get snapshots by camera
     */
    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<PageResponse<SnapshotImageResponse>>> getSnapshotsByCamera(
            @PathVariable Long cameraId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<SnapshotImageResponse> snapshots = snapshotService.getSnapshotsByCamera(cameraId, page, size);
        return ResponseEntity.ok(ApiResponse.success(snapshots));
    }

    /**
     * Get latest snapshot của camera
     */
    @GetMapping("/camera/{cameraId}/latest")
    public ResponseEntity<ApiResponse<SnapshotImageResponse>> getLatestSnapshot(@PathVariable Long cameraId) {
        SnapshotImageResponse snapshot = snapshotService.getLatestSnapshot(cameraId);
        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    /**
     * Get snapshot image file
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getSnapshotImage(@PathVariable Long id) {
        SnapshotImageResponse snapshotInfo = snapshotService.getSnapshotById(id);
        Resource image = snapshotService.viewImage(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(image);
    }

    /**
     * Get thumbnail
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "200") Integer width,
            @RequestParam(defaultValue = "200") Integer height) {
        Resource thumbnail = snapshotService.getThumbnail(id, width, height);
        
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(thumbnail);
    }

    /**
     * Download snapshot
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSnapshot(@PathVariable Long id) {
        SnapshotImageResponse snapshotInfo = snapshotService.getSnapshotById(id);
        Resource image = snapshotService.downloadImage(id);
        
        String filename = "snapshot_" + id + ".jpg";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"")
                .body(image);
    }

    /**
     * Delete snapshot
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSnapshot(@PathVariable Long id) {
        snapshotService.deleteSnapshot(id);
        return ResponseEntity.ok(ApiResponse.success("Snapshot deleted successfully", null));
    }

    /**
     * Cleanup old snapshots
     */
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldSnapshots(
            @RequestParam(defaultValue = "30") int retentionDays) {
        int deletedCount = snapshotService.cleanupOldSnapshots(retentionDays);
        return ResponseEntity.ok(ApiResponse.success("Cleanup completed", deletedCount));
    }

    /**
     * Batch delete snapshots
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Integer>> batchDeleteSnapshots(
            @RequestBody List<Long> ids) {
        int deletedCount = 0;
        for (Long id : ids) {
            try {
                snapshotService.deleteSnapshot(id);
                deletedCount++;
            } catch (Exception e) {
                // Continue with next
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Batch delete completed", deletedCount));
    }
}
