package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.SystemLog;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.repository.SystemLogRepository;
import com.example.camerasurveillancesystem.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;

    @Override
    @Transactional
    public void logInfo(String module, String message, String source) {
        saveLog("INFO", module, message, null, source);
    }

    @Override
    @Transactional
    public void logWarn(String module, String message, String source) {
        saveLog("WARNING", module, message, null, source);
    }

    @Override
    @Transactional
    public void logError(String module, String message, String details, String source) {
        saveLog("ERROR", module, message, details, source);
    }

    @Override
    @Transactional
    public void logDebug(String module, String message, String source) {
        saveLog("DEBUG", module, message, null, source);
    }

    private void saveLog(String level, String module, String message, String details, String source) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setLevel(level);
            systemLog.setModule(module);
            systemLog.setMessage(message);
            systemLog.setDetails(details);
            systemLog.setCreatedAt(LocalDateTime.now());

            systemLogRepository.save(systemLog);
        } catch (Exception e) {
            // Prevent logging errors from breaking the application
            log.error("Failed to save system log: {}", e.getMessage());
        }
    }

    @Override
    public PageResponse<Map<String, Object>> getLogs(
            String level, 
            String module, 
            LocalDateTime start, 
            LocalDateTime end, 
            Pageable pageable
    ) {
        Page<SystemLog> logPage;

        if (level != null && !level.isEmpty()) {
            logPage = systemLogRepository.findByLevel(level, pageable);
        } else if (module != null && !module.isEmpty()) {
            logPage = systemLogRepository.findByModule(module, pageable);
        } else if (start != null && end != null) {
            logPage = systemLogRepository.findByCreatedAtBetween(start, end, pageable);
        } else {
            logPage = systemLogRepository.findAll(pageable);
        }

        return PageResponse.<Map<String, Object>>builder()
                .content(logPage.getContent().stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList()))
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }

    @Override
    public PageResponse<Map<String, Object>> getRecentErrors(Pageable pageable) {
        Page<SystemLog> errorPage = systemLogRepository.findRecentErrors(pageable);

        return PageResponse.<Map<String, Object>>builder()
                .content(errorPage.getContent().stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList()))
                .pageNumber(errorPage.getNumber())
                .pageSize(errorPage.getSize())
                .totalElements(errorPage.getTotalElements())
                .totalPages(errorPage.getTotalPages())
                .last(errorPage.isLast())
                .build();
    }

    @Override
    public Map<String, Object> getLogStatistics(LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();

        // Count by level
        Map<String, Long> levelStats = new HashMap<>();
        var groupedByLevel = systemLogRepository.countByLevelGrouped(since);
        for (Object[] row : groupedByLevel) {
            levelStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("byLevel", levelStats);

        // Count by module
        Map<String, Long> moduleStats = new HashMap<>();
        var groupedByModule = systemLogRepository.countByModuleGrouped(since);
        for (Object[] row : groupedByModule) {
            moduleStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("byModule", moduleStats);

        // Total counts
        stats.put("totalErrors", levelStats.getOrDefault("ERROR", 0L) + levelStats.getOrDefault("CRITICAL", 0L));
        stats.put("totalWarnings", levelStats.getOrDefault("WARNING", 0L));
        stats.put("totalLogs", levelStats.values().stream().mapToLong(Long::longValue).sum());

        return stats;
    }

    @Override
    @Transactional
    public void deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        systemLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Deleted system logs older than {} days", daysToKeep);
    }

    private Map<String, Object> convertToMap(SystemLog systemLog) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", systemLog.getId());
        map.put("level", systemLog.getLevel());
        map.put("module", systemLog.getModule());
        map.put("message", systemLog.getMessage());
        map.put("details", systemLog.getDetails());
        map.put("createdAt", systemLog.getCreatedAt());
        return map;
    }
}
