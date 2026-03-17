package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.User;
import com.example.camerasurveillancesystem.domain.UserActivityLog;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.UserActivityLogResponse;
import com.example.camerasurveillancesystem.repository.UserActivityLogRepository;
import com.example.camerasurveillancesystem.repository.UserRepository;
import com.example.camerasurveillancesystem.service.UserActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogRepository logRepository;
    private final UserRepository userRepository;

    @Override
    public void logActivity(String username, String action, String resourceType, Long resourceId,
                           String description, HttpServletRequest request) {
        logActivity(username, action, resourceType, resourceId, description, "SUCCESS", 0L, request);
    }

    @Override
    @Async
    @Transactional
    public void logActivity(String username, String action, String resourceType, Long resourceId,
                           String description, String status, Long executionTime, HttpServletRequest request) {
        try {
            UserActivityLog log = new UserActivityLog();
            
            // Set user
            if (username != null) {
                userRepository.findByUsername(username).ifPresent(log::setUser);
                log.setUsername(username);
            }
            
            log.setAction(action);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setDescription(description);
            log.setStatus(status);
            log.setExecutionTime(executionTime);
            
            // Extract request info
            if (request != null) {
                log.setIpAddress(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setHttpMethod(request.getMethod());
                log.setRequestUrl(request.getRequestURI());
            }
            
            logRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
    public void logActivity(String username, String action, String resourceType, Long resourceId,
                           String description, String oldValue, String newValue, HttpServletRequest request) {
        try {
            UserActivityLog log = new UserActivityLog();
            
            if (username != null) {
                userRepository.findByUsername(username).ifPresent(log::setUser);
                log.setUsername(username);
            }
            
            log.setAction(action);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setDescription(description);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setStatus("SUCCESS");
            
            if (request != null) {
                log.setIpAddress(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setHttpMethod(request.getMethod());
                log.setRequestUrl(request.getRequestURI());
            }
            
            logRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }

    @Override
    public PageResponse<UserActivityLogResponse> getAllLogs(Pageable pageable) {
        Page<UserActivityLog> page = logRepository.findAll(pageable);
        return convertToPageResponse(page);
    }

    @Override
    public PageResponse<UserActivityLogResponse> getLogsByUser(Long userId, Pageable pageable) {
        Page<UserActivityLog> page = logRepository.findByUserId(userId, pageable);
        return convertToPageResponse(page);
    }

    @Override
    public PageResponse<UserActivityLogResponse> getLogsByAction(String action, Pageable pageable) {
        Page<UserActivityLog> page = logRepository.findByAction(action, pageable);
        return convertToPageResponse(page);
    }

    @Override
    public PageResponse<UserActivityLogResponse> getLogsByResourceType(String resourceType, Pageable pageable) {
        Page<UserActivityLog> page = logRepository.findByResourceType(resourceType, pageable);
        return convertToPageResponse(page);
    }

    @Override
    public PageResponse<UserActivityLogResponse> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<UserActivityLog> page = logRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return convertToPageResponse(page);
    }

    @Override
    public List<UserActivityLogResponse> getRecentLogs() {
        return logRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserActivityLogResponse> getFailedOperations(int limit) {
        return logRepository.findRecentFailedOperations(PageRequest.of(0, limit)).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countActivitiesByUser(Long userId) {
        return logRepository.countByUserId(userId);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private UserActivityLogResponse convertToResponse(UserActivityLog log) {
        return UserActivityLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUsername())
                .action(log.getAction())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .httpMethod(log.getHttpMethod())
                .requestUrl(log.getRequestUrl())
                .responseStatus(log.getResponseStatus())
                .executionTime(log.getExecutionTime())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private PageResponse<UserActivityLogResponse> convertToPageResponse(Page<UserActivityLog> page) {
        return PageResponse.<UserActivityLogResponse>builder()
                .content(page.getContent().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
