package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelCreateRequest;
import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.NotificationChannelResponse;

import java.util.List;

public interface NotificationChannelService {

    /**
     * Tạo channel mới
     */
    NotificationChannelResponse createChannel(NotificationChannelCreateRequest request);

    /**
     * Update channel
     */
    NotificationChannelResponse updateChannel(Long id, NotificationChannelUpdateRequest request);

    /**
     * Get channel by ID
     */
    NotificationChannelResponse getChannelById(Long id);

    /**
     * Get all channels
     */
    PageResponse<NotificationChannelResponse> getAllChannels(Integer page, Integer size);

    /**
     * Get active channels
     */
    List<NotificationChannelResponse> getActiveChannels();

    /**
     * Get channels by type
     */
    List<NotificationChannelResponse> getChannelsByType(String channelType);

    /**
     * Get default channels
     */
    List<NotificationChannelResponse> getDefaultChannels();

    /**
     * Test channel
     */
    boolean testChannel(Long id, String testRecipient);

    /**
     * Toggle channel active status
     */
    NotificationChannelResponse toggleChannelStatus(Long id);

    /**
     * Delete channel
     */
    void deleteChannel(Long id);
}
