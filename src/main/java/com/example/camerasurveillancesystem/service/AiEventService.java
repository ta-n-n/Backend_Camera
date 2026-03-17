package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.ai.AiEventSearchRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.ai.AiEventResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface AiEventService {

    /**
     * Get event by ID
     */
    AiEventResponse getEventById(Long id);

    /**
     * Search events with filters
     */
    PageResponse<AiEventResponse> searchEvents(AiEventSearchRequest request);

    /**
     * Get events by camera
     */
    List<AiEventResponse> getEventsByCamera(Long cameraId);

    /**
     * Get events by model
     */
    List<AiEventResponse> getEventsByModel(Long modelId);

    /**
     * Get events by type
     */
    List<AiEventResponse> getEventsByType(String eventType);

    /**
     * Get events by camera and date range
     */
    List<AiEventResponse> getEventsByCameraAndDateRange(Long cameraId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get recent events (last N hours)
     */
    List<AiEventResponse> getRecentEvents(int hours);

    /**
     * Get events with high confidence
     */
    List<AiEventResponse> getHighConfidenceEvents(Double minConfidence);

    /**
     * Count events by type
     */
    long countEventsByType(String eventType);

    /**
     * Delete event
     */
    void deleteEvent(Long id);
}
