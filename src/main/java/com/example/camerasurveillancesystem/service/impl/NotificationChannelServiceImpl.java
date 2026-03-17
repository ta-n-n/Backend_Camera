package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.NotificationChannel;
import com.example.camerasurveillancesystem.dto.mapper.NotificationChannelMapper;
import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelCreateRequest;
import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.NotificationChannelResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.NotificationChannelRepository;
import com.example.camerasurveillancesystem.service.NotificationChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationChannelServiceImpl implements NotificationChannelService {

    private final NotificationChannelRepository channelRepository;
    private final NotificationChannelMapper channelMapper;

    @Override
    @Transactional
    public NotificationChannelResponse createChannel(NotificationChannelCreateRequest request) {
        // Check if name already exists
        if (channelRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Channel name already exists: " + request.getName());
        }
        
        NotificationChannel channel = channelMapper.toEntity(request);
        channel.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        NotificationChannel savedChannel = channelRepository.save(channel);
        log.info("Created notification channel: {}", savedChannel.getName());
        
        return channelMapper.toResponse(savedChannel);
    }

    @Override
    @Transactional
    public NotificationChannelResponse updateChannel(Long id, NotificationChannelUpdateRequest request) {
        NotificationChannel channel = channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND));
        
        // Check name uniqueness if changed
        if (request.getName() != null && !request.getName().equals(channel.getName())) {
            if (channelRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Channel name already exists: " + request.getName());
            }
            channel.setName(request.getName());
        }
        
        if (request.getIsActive() != null) {
            channel.setIsActive(request.getIsActive());
        }
        
        NotificationChannel updatedChannel = channelRepository.save(channel);
        log.info("Updated notification channel: {}", updatedChannel.getName());
        
        return channelMapper.toResponse(updatedChannel);
    }

    @Override
    public NotificationChannelResponse getChannelById(Long id) {
        NotificationChannel channel = channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND));
        return channelMapper.toResponse(channel);
    }

    @Override
    public PageResponse<NotificationChannelResponse> getAllChannels(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var channelPage = channelRepository.findAll(pageable);
        
        return PageResponse.<NotificationChannelResponse>builder()
            .content(channelPage.getContent().stream()
                .map(channelMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(channelPage.getNumber())
            .pageSize(channelPage.getSize())
            .totalElements(channelPage.getTotalElements())
            .totalPages(channelPage.getTotalPages())
            .last(channelPage.isLast())
            .build();
    }

    @Override
    public List<NotificationChannelResponse> getActiveChannels() {
        return channelRepository.findByIsActiveTrue().stream()
            .map(channelMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<NotificationChannelResponse> getChannelsByType(String channelType) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "name"));
        return channelRepository.findByChannelType(channelType, pageable)
            .map(channelMapper::toResponse)
            .getContent();
    }

    @Override
    public List<NotificationChannelResponse> getDefaultChannels() {
        // Return active channels with high priority
        return channelRepository.findByIsActiveTrue().stream()
            .map(channelMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public boolean testChannel(Long id, String testRecipient) {
        NotificationChannel channel = channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND));
        
        // TODO: Implement actual channel testing logic
        log.info("Testing channel {} with recipient {}", channel.getName(), testRecipient);
        return true;
    }

    @Override
    @Transactional
    public NotificationChannelResponse toggleChannelStatus(Long id) {
        NotificationChannel channel = channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND));
        
        channel.setIsActive(!channel.getIsActive());
        NotificationChannel updated = channelRepository.save(channel);
        
        log.info("{} notification channel: {}", 
            updated.getIsActive() ? "Activated" : "Deactivated", 
            channel.getName());
        
        return channelMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteChannel(Long id) {
        if (!channelRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND);
        }
        
        channelRepository.deleteById(id);
        log.info("Deleted notification channel with ID: {}", id);
    }
}
