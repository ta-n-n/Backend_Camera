package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.service.TestDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/test-data")
@RequiredArgsConstructor
@Slf4j
public class TestDataController {

    private final TestDataService testDataService;

    /**
     * Seed 50 fake cameras with locations and groups
     * POST /api/v1/admin/test-data/cameras/seed
     */
    @PostMapping("/cameras/seed")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedCameras(
            @RequestParam(defaultValue = "50") int count
    ) {
        log.info("Request to seed {} fake cameras", count);
        
        Map<String, Long> result = testDataService.seedCameras(count);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully seeded test data");
        response.put("cameras_created", result.get("cameras"));
        response.put("locations_created", result.get("locations"));
        response.put("groups_created", result.get("groups"));
        response.put("total_cameras", result.get("total"));
        
        return ResponseEntity.ok(ApiResponse.success("Seed camera data thành công", response));
    }

    /**
     * Clear all test cameras (cameras with code starting with "CAM-")
     * DELETE /api/v1/admin/test-data/cameras/clear
     */
    @DeleteMapping("/cameras/clear")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> clearTestCameras() {
        log.info("Request to clear all test cameras");
        
        long deletedCount = testDataService.clearTestCameras();
        
        Map<String, Long> response = new HashMap<>();
        response.put("deleted_cameras", deletedCount);
        
        return ResponseEntity.ok(ApiResponse.success("Xóa test cameras thành công", response));
    }

    /**
     * Get camera statistics
     * GET /api/v1/admin/test-data/cameras/stats
     */
    @GetMapping("/cameras/stats")
    @PreAuthorize("hasAuthority('CAMERA_READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCameraStats() {
        log.info("Request to get camera statistics");
        
        Map<String, Object> stats = testDataService.getCameraStatistics();
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Reset all cameras to default state
     * POST /api/v1/admin/test-data/cameras/reset
     */
    @PostMapping("/cameras/reset")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetCameras() {
        log.info("Request to reset all cameras");
        
        testDataService.resetAllCameras();
        
        return ResponseEntity.ok(ApiResponse.success("Reset tất cả cameras thành công", null));
    }
}
