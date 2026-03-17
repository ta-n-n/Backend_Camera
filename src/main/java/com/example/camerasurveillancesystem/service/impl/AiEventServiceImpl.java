package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.AiEvent;
import com.example.camerasurveillancesystem.dto.mapper.AiEventMapper;
import com.example.camerasurveillancesystem.dto.request.ai.AiEventSearchRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.ai.AiEventResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.AiEventRepository;
import com.example.camerasurveillancesystem.service.AiEventService;
import com.example.camerasurveillancesystem.specification.AiEventSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiEventServiceImpl implements AiEventService {

    private final AiEventRepository eventRepository;
    private final AiEventMapper eventMapper;

    @Override
    public AiEventResponse getEventById(Long id) {
        AiEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        return eventMapper.toResponse(event);
    }

    @Override
    public PageResponse<AiEventResponse> searchEvents(AiEventSearchRequest request) {
        Specification<AiEvent> spec = Specification.where(null);

        if (request.getCameraId() != null) {
            spec = spec.and(AiEventSpecification.hasCameraId(request.getCameraId()));
        }
        if (request.getModelId() != null) {
            spec = spec.and(AiEventSpecification.hasModelId(request.getModelId()));
        }
        if (request.getEventType() != null) {
            spec = spec.and(AiEventSpecification.hasEventType(request.getEventType()));
        }
        if (request.getMinConfidence() != null) {
            spec = spec.and(AiEventSpecification.hasMinConfidence(request.getMinConfidence()));
        }
        if (request.getStartDate() != null && request.getEndDate() != null) {
            spec = spec.and(AiEventSpecification.detectedBetween(request.getStartDate(), request.getEndDate()));
        }

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectedAt"));
        var eventPage = eventRepository.findAll(spec, pageable);

        return PageResponse.<AiEventResponse>builder()
                .content(eventPage.getContent().stream()
                        .map(eventMapper::toResponse)
                        .collect(Collectors.toList()))
                .pageNumber(eventPage.getNumber())
                .pageSize(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .last(eventPage.isLast())
                .build();
    }

    @Override
    public List<AiEventResponse> getEventsByCamera(Long cameraId) {
        return eventRepository.findByCameraId(cameraId).stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventResponse> getEventsByModel(Long modelId) {
        return eventRepository.findByModelId(modelId).stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventResponse> getEventsByType(String eventType) {
        return eventRepository.findByEventType(eventType).stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventResponse> getEventsByCameraAndDateRange(Long cameraId, LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findByCameraIdAndDateRange(cameraId, startDate, endDate).stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventResponse> getRecentEvents(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return eventRepository.findRecentEvents(since).stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventResponse> getHighConfidenceEvents(Double minConfidence) {
        Specification<AiEvent> spec = AiEventSpecification.hasMinConfidence(minConfidence);
        return eventRepository.findAll(spec).stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countEventsByType(String eventType) {
        return eventRepository.countByEventType(eventType);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND);
        }

        eventRepository.deleteById(id);
        log.info("Deleted AI event {}", id);
    }
}
