package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.ai.AiEventSearchRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.ai.AiEventResponse;
import com.example.camerasurveillancesystem.service.AiEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-events")
@RequiredArgsConstructor
public class AiEventController {

    private final AiEventService eventService;

    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiEventResponse>> getEventById(@PathVariable Long id) {
        AiEventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    /**
     * Search events with filters
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AiEventResponse>>> searchEvents(
            @ModelAttribute AiEventSearchRequest request) {
        PageResponse<AiEventResponse> events = eventService.searchEvents(request);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get events by camera
     */
    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<ApiResponse<List<AiEventResponse>>> getEventsByCamera(@PathVariable Long cameraId) {
        List<AiEventResponse> events = eventService.getEventsByCamera(cameraId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get events by model
     */
    @GetMapping("/model/{modelId}")
    public ResponseEntity<ApiResponse<List<AiEventResponse>>> getEventsByModel(@PathVariable Long modelId) {
        List<AiEventResponse> events = eventService.getEventsByModel(modelId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get events by type
     */
    @GetMapping("/type/{eventType}")
    public ResponseEntity<ApiResponse<List<AiEventResponse>>> getEventsByType(@PathVariable String eventType) {
        List<AiEventResponse> events = eventService.getEventsByType(eventType);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get events by camera and date range
     */
    @GetMapping("/camera/{cameraId}/date-range")
    public ResponseEntity<ApiResponse<List<AiEventResponse>>> getEventsByCameraAndDateRange(
            @PathVariable Long cameraId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AiEventResponse> events = eventService.getEventsByCameraAndDateRange(cameraId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get recent events (last N hours)
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AiEventResponse>>> getRecentEvents(
            @RequestParam(defaultValue = "24") int hours) {
        List<AiEventResponse> events = eventService.getRecentEvents(hours);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Get high confidence events
     */
    @GetMapping("/high-confidence")
    public ResponseEntity<ApiResponse<List<AiEventResponse>>> getHighConfidenceEvents(
            @RequestParam(defaultValue = "0.8") Double minConfidence) {
        List<AiEventResponse> events = eventService.getHighConfidenceEvents(minConfidence);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Count events by type
     */
    @GetMapping("/count/type/{eventType}")
    public ResponseEntity<ApiResponse<Long>> countEventsByType(@PathVariable String eventType) {
        long count = eventService.countEventsByType(eventType);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Delete event
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("AI event deleted successfully", null));
    }
}
