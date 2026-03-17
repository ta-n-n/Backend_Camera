package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.AiEvent;
import com.example.camerasurveillancesystem.domain.Alert;
import com.example.camerasurveillancesystem.dto.mapper.AlertMapper;
import com.example.camerasurveillancesystem.dto.request.alert.AlertCreateRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertSearchRequest;
import com.example.camerasurveillancesystem.dto.request.alert.AlertUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertStatistics;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.AiEventRepository;
import com.example.camerasurveillancesystem.repository.AlertRepository;
import com.example.camerasurveillancesystem.service.AlertService;
import com.example.camerasurveillancesystem.specification.AlertSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AiEventRepository aiEventRepository;
    private final AlertMapper alertMapper;

    @Override
    @Transactional
    public AlertResponse createAlert(AlertCreateRequest request) {
        Alert alert = alertMapper.toEntity(request);
        
        // Set AI event if provided
        if (request.getAiEventId() != null) {
            AiEvent aiEvent = aiEventRepository.findById(request.getAiEventId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AI_EVENT_NOT_FOUND));
            alert.setAiEvent(aiEvent);
        }
        
        // Set default status
        if (alert.getStatus() == null) {
            alert.setStatus("PENDING");
        }
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Created alert {} with severity {}", savedAlert.getId(), savedAlert.getSeverity());
        
        return alertMapper.toResponse(savedAlert);
    }

    @Override
    @Transactional
    public AlertResponse updateAlert(Long id, AlertUpdateRequest request) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        
        if (request.getStatus() != null) {
            alert.setStatus(request.getStatus());
        }
        
        if (request.getSeverity() != null) {
            alert.setSeverity(request.getSeverity());
        }
        
        if (request.getResolutionNotes() != null) {
            alert.setDescription(alert.getDescription() + "\n\nNotes: " + request.getResolutionNotes());
        }
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Updated alert {}", id);
        
        return alertMapper.toResponse(updatedAlert);
    }

    @Override
    @Transactional
    public AlertResponse acknowledgeAlert(Long id, Long userId) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        
        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledgedAt(LocalDateTime.now());
        
        // TODO: Set acknowledgedBy user when User module is implemented
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Acknowledged alert {}", id);
        
        return alertMapper.toResponse(updatedAlert);
    }

    @Override
    @Transactional
    public AlertResponse assignAlert(Long id, Long userId) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        
        // TODO: Set assignedTo user when User module is implemented
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Assigned alert {} to user {}", id, userId);
        
        return alertMapper.toResponse(updatedAlert);
    }

    @Override
    @Transactional
    public AlertResponse resolveAlert(Long id, String resolutionNotes) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        
        if (resolutionNotes != null) {
            alert.setDescription(alert.getDescription() + "\n\nResolution: " + resolutionNotes);
        }
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Resolved alert {}", id);
        
        return alertMapper.toResponse(updatedAlert);
    }

    @Override
    @Transactional
    public AlertResponse markAsFalsePositive(Long id, String reason) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        
        alert.setStatus("DISMISSED");
        
        if (reason != null) {
            alert.setDescription(alert.getDescription() + "\n\nFalse Positive Reason: " + reason);
        }
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Marked alert {} as false positive", id);
        
        return alertMapper.toResponse(updatedAlert);
    }

    @Override
    public AlertResponse getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        return alertMapper.toResponse(alert);
    }

    @Override
    public PageResponse<AlertResponse> searchAlerts(AlertSearchRequest request) {
        Specification<Alert> spec = Specification.where(null);
        
        if (request.getCameraId() != null) {
            spec = spec.and(AlertSpecification.hasCameraId(request.getCameraId()));
        }
        
        if (request.getAlertType() != null) {
            spec = spec.and(AlertSpecification.hasAlertType(request.getAlertType()));
        }
        
        if (request.getSeverity() != null) {
            spec = spec.and(AlertSpecification.hasSeverity(request.getSeverity()));
        }
        
        if (request.getStatus() != null) {
            spec = spec.and(AlertSpecification.hasStatus(request.getStatus()));
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            spec = spec.and(AlertSpecification.createdBetween(request.getStartDate(), request.getEndDate()));
        }
        
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var alertPage = alertRepository.findAll(spec, pageable);
        
        return PageResponse.<AlertResponse>builder()
            .content(alertPage.getContent().stream()
                .map(alertMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(alertPage.getNumber())
            .pageSize(alertPage.getSize())
            .totalElements(alertPage.getTotalElements())
            .totalPages(alertPage.getTotalPages())
            .last(alertPage.isLast())
            .build();
    }

    @Override
    public List<AlertResponse> getUnresolvedAlerts(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return alertRepository.findUnresolvedAlerts(pageable).stream()
            .map(alertMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AlertResponse> getCriticalUnresolvedAlerts() {
        Specification<Alert> spec = AlertSpecification.isUnresolved()
            .and(AlertSpecification.isCritical());
        
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        return alertRepository.findAll(spec, pageable).stream()
            .map(alertMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AlertResponse> getAlertsByCamera(Long cameraId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var alertPage = alertRepository.findByCameraId(cameraId, pageable);
        
        return PageResponse.<AlertResponse>builder()
            .content(alertPage.getContent().stream()
                .map(alertMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(alertPage.getNumber())
            .pageSize(alertPage.getSize())
            .totalElements(alertPage.getTotalElements())
            .totalPages(alertPage.getTotalPages())
            .last(alertPage.isLast())
            .build();
    }

    @Override
    public AlertStatistics getAlertStatistics() {
        AlertStatistics statistics = new AlertStatistics();
        
        // Count by status
        statistics.setTotalAlerts(alertRepository.count());
        statistics.setNewAlerts(alertRepository.countByStatus("NEW") + alertRepository.countByStatus("PENDING"));
        statistics.setAcknowledgedAlerts(alertRepository.countByStatus("ACKNOWLEDGED"));
        statistics.setResolvedAlerts(alertRepository.countByStatus("RESOLVED"));
        statistics.setFalsePositiveAlerts(alertRepository.countByStatus("DISMISSED"));
        
        // Count by severity
        statistics.setCriticalAlerts(alertRepository.countBySeverity("CRITICAL"));
        statistics.setHighAlerts(alertRepository.countBySeverity("HIGH"));
        statistics.setMediumAlerts(alertRepository.countBySeverity("MEDIUM"));
        statistics.setLowAlerts(alertRepository.countBySeverity("LOW"));
        
        // Placeholders for grouped statistics
        statistics.setAlertsByType(new HashMap<>());
        statistics.setAlertsByCamera(new HashMap<>());
        
        return statistics;
    }

    @Override
    @Transactional
    public void deleteAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND);
        }
        
        alertRepository.deleteById(id);
        log.info("Deleted alert {}", id);
    }

    @Override
    @Transactional
    public void deleteMultipleAlerts(List<Long> ids) {
        List<Alert> alerts = alertRepository.findAllById(ids);

        if (alerts.size() != ids.size()) {
            Set<Long> foundIds = alerts.stream()
                    .map(Alert::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException(
                    ErrorCode.ALERT_NOT_FOUND,
                    "Một số cảnh báo không tồn tại: " + missingIds
            );
        }

        alertRepository.deleteAllById(ids);
        log.info("Deleted {} alerts: {}", ids.size(), ids);
    }
}
