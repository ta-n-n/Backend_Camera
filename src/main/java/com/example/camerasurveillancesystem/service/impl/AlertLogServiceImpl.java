package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Alert;
import com.example.camerasurveillancesystem.domain.AlertLog;
import com.example.camerasurveillancesystem.dto.mapper.AlertLogMapper;
import com.example.camerasurveillancesystem.dto.request.alert.AlertLogCreateRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.alert.AlertLogResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.AlertLogRepository;
import com.example.camerasurveillancesystem.repository.AlertRepository;
import com.example.camerasurveillancesystem.service.AlertLogService;
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
public class AlertLogServiceImpl implements AlertLogService {

    private final AlertLogRepository alertLogRepository;
    private final AlertRepository alertRepository;
    private final AlertLogMapper alertLogMapper;

    @Override
    @Transactional
    public AlertLogResponse createLog(AlertLogCreateRequest request) {
        AlertLog alertLog = alertLogMapper.toEntity(request);
        
        // Set alert
        Alert alert = alertRepository.findById(request.getAlertId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ALERT_NOT_FOUND));
        alertLog.setAlert(alert);
        
        // TODO: Set user when User module is implemented
        
        AlertLog savedLog = alertLogRepository.save(alertLog);
        log.info("Created alert log for alert {}", alert.getId());
        
        return alertLogMapper.toResponse(savedLog);
    }

    @Override
    public List<AlertLogResponse> getLogsByAlertId(Long alertId) {
        return alertLogRepository.findByAlertIdOrderByCreatedAtDesc(alertId).stream()
            .map(alertLogMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AlertLogResponse> getLogsByUserId(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var logPage = alertLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return PageResponse.<AlertLogResponse>builder()
            .content(logPage.getContent().stream()
                .map(alertLogMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(logPage.getNumber())
            .pageSize(logPage.getSize())
            .totalElements(logPage.getTotalElements())
            .totalPages(logPage.getTotalPages())
            .last(logPage.isLast())
            .build();
    }

    @Override
    public List<AlertLogResponse> getRecentLogs(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return alertLogRepository.findAll(pageable).stream()
            .map(alertLogMapper::toResponse)
            .collect(Collectors.toList());
    }
}
