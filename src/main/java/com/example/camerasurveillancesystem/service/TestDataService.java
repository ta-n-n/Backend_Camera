package com.example.camerasurveillancesystem.service;

import java.util.Map;

public interface TestDataService {

    /**
     * Seed fake cameras for testing
     * @param count Number of cameras to create
     * @return Map with counts of created entities
     */
    Map<String, Long> seedCameras(int count);

    /**
     * Clear all test cameras (code starts with "CAM-")
     * @return Number of deleted cameras
     */
    long clearTestCameras();

    /**
     * Get camera statistics
     * @return Map with statistics
     */
    Map<String, Object> getCameraStatistics();

    /**
     * Reset all cameras to ACTIVE status
     */
    void resetAllCameras();
}
