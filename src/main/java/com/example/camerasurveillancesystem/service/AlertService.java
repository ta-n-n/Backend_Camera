package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.alert.AlertCreateRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertSearchRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertStatistics;

import java.util.List;

public interface AlertService {

    /**
     * Tạo alert mới
     */
    AlertResponse createAlert(AlertCreateRequest request);

    /**
     * Update alert
     */
    AlertResponse updateAlert(Long id, AlertUpdateRequest request);

    /**
     * Acknowledge alert
     */
    AlertResponse acknowledgeAlert(Long id, Long userId);

    /**
     * Assign alert to user
     */
    AlertResponse assignAlert(Long id, Long userId);

    /**
     * Resolve alert
     */
    AlertResponse resolveAlert(Long id, String resolutionNotes);

    /**
     * Mark as false positive
     */
    AlertResponse markAsFalsePositive(Long id, String reason);

    /**
     * Get alert by ID
     */
    AlertResponse getAlertById(Long id);

    /**
     * Search alerts
     */
    PageResponse<AlertResponse> searchAlerts(AlertSearchRequest request);

    /**
     * Get unresolved alerts
     */
    List<AlertResponse> getUnresolvedAlerts(Integer limit);

    /**
     * Get critical unresolved alerts
     */
    List<AlertResponse> getCriticalUnresolvedAlerts();

    /**
     * Get alerts by camera
     */
    PageResponse<AlertResponse> getAlertsByCamera(Long cameraId, Integer page, Integer size);

    /**
     * Get alert statistics
     */
    AlertStatistics getAlertStatistics();

    /**
     * Delete alert
     */
    void deleteAlert(Long id);

    /**
     * Delete multiple alerts
     */
    void deleteMultipleAlerts(List<Long> ids);
}
