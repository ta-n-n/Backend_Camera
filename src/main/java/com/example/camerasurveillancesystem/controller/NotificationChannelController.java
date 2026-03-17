package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelCreateRequest;
import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.NotificationChannelResponse;
import com.example.camerasurveillancesystem.service.NotificationChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-channels")
@RequiredArgsConstructor
public class NotificationChannelController {

    private final NotificationChannelService channelService;

    /**
     * Tạo notification channel mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> createChannel(
            @Valid @RequestBody NotificationChannelCreateRequest request) {
        NotificationChannelResponse channel = channelService.createChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Channel created successfully", channel));
    }

    /**
     * Cập nhật channel
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody NotificationChannelUpdateRequest request) {
        NotificationChannelResponse channel = channelService.updateChannel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Channel updated successfully", channel));
    }

    /**
     * Toggle channel active status
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> toggleChannelStatus(
            @PathVariable Long id) {
        NotificationChannelResponse channel = channelService.toggleChannelStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Channel status toggled", channel));
    }

    /**
     * Test channel
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<Boolean>> testChannel(
            @PathVariable Long id,
            @RequestParam String testRecipient) {
        boolean result = channelService.testChannel(id, testRecipient);
        return ResponseEntity.ok(ApiResponse.success("Channel tested", result));
    }

    /**
     * Get channel by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationChannelResponse>> getChannelById(
            @PathVariable Long id) {
        NotificationChannelResponse channel = channelService.getChannelById(id);
        return ResponseEntity.ok(ApiResponse.success(channel));
    }

    /**
     * Get all channels
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationChannelResponse>>> getAllChannels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<NotificationChannelResponse> channels = 
                channelService.getAllChannels(page, size);
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    /**
     * Get active channels
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<NotificationChannelResponse>>> getActiveChannels() {
        List<NotificationChannelResponse> channels = channelService.getActiveChannels();
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    /**
     * Get channels theo type
     */
    @GetMapping("/type/{channelType}")
    public ResponseEntity<ApiResponse<List<NotificationChannelResponse>>> getChannelsByType(
            @PathVariable String channelType) {
        List<NotificationChannelResponse> channels = channelService.getChannelsByType(channelType);
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    /**
     * Get default channels
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<List<NotificationChannelResponse>>> getDefaultChannels() {
        List<NotificationChannelResponse> channels = channelService.getDefaultChannels();
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    /**
     * Delete channel
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChannel(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return ResponseEntity.ok(ApiResponse.success("Channel deleted successfully", null));
    }
}
