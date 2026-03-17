package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraHealthLog;
import com.example.camerasurveillancesystem.dto.mapper.CameraHealthLogMapper;
import com.example.camerasurveillancesystem.dto.request.CameraHealthLogCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraHealthLogSearchRequest;
import com.example.camerasurveillancesystem.dto.response.CameraHealthLogResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.exception.*;
import com.example.camerasurveillancesystem.repository.CameraHealthLogRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.CameraHealthLogService;
import com.example.camerasurveillancesystem.specification.CameraHealthLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CameraHealthLogServiceImpl implements CameraHealthLogService {

    private final CameraHealthLogRepository healthLogRepository;
    private final CameraRepository cameraRepository;
    private final CameraHealthLogMapper healthLogMapper;

    @Override
    public CameraHealthLogResponse createHealthLog(CameraHealthLogCreateRequest request) {
        log.info("Creating health log for camera ID: {}", request.getCameraId());

        Camera camera = cameraRepository.findById(request.getCameraId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy camera với ID: " + request.getCameraId()
                ));

        CameraHealthLog healthLog = new CameraHealthLog();
        healthLog.setCamera(camera);
        healthLog.setStatus(request.getStatus());
        healthLog.setMessage(request.getMessage());
        healthLog.setCpuUsage(request.getCpuUsage());
        healthLog.setMemoryUsage(request.getMemoryUsage());
        healthLog.setDiskUsage(request.getDiskUsage());
        healthLog.setTemperature(request.getTemperature());
        healthLog.setBandwidth(request.getBandwidth());
        healthLog.setErrorCode(request.getErrorCode());

        CameraHealthLog savedLog = healthLogRepository.save(healthLog);
        log.info("Health log created successfully with ID: {}", savedLog.getId());

        return healthLogMapper.toResponse(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public CameraHealthLogResponse getHealthLogById(Long id) {
        log.info("Getting health log by ID: {}", id);

        CameraHealthLog healthLog = healthLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy health log với ID: " + id
                ));

        return healthLogMapper.toResponse(healthLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraHealthLogResponse> getAllHealthLogs() {
        log.info("Getting all health logs");

        List<CameraHealthLog> logs = healthLogRepository.findAll();

        return logs.stream()
                .map(healthLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraHealthLogResponse> getHealthLogsByCameraId(Long cameraId) {
        log.info("Getting health logs for camera ID: {}", cameraId);

        if (!cameraRepository.existsById(cameraId)) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy camera với ID: " + cameraId
            );
        }

        List<CameraHealthLog> logs = healthLogRepository.findByCameraId(cameraId);

        return logs.stream()
                .map(healthLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraHealthLogResponse> getHealthLogsByStatus(String status) {
        log.info("Getting health logs by status: {}", status);

        List<CameraHealthLog> logs = healthLogRepository.findByStatus(status);

        return logs.stream()
                .map(healthLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CameraHealthLogResponse> searchHealthLogs(CameraHealthLogSearchRequest request) {
        log.info("Searching health logs with filters");

        Specification<CameraHealthLog> spec = CameraHealthLogSpecification.withFilters(request);

        Sort sort = request.getSortDirection().equalsIgnoreCase("DESC")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<CameraHealthLog> logPage = healthLogRepository.findAll(spec, pageable);

        List<CameraHealthLogResponse> content = logPage.getContent().stream()
                .map(healthLogMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<CameraHealthLogResponse>builder()
                .content(content)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .first(logPage.isFirst())
                .build();
    }

    @Override
    public void deleteHealthLog(Long id) {
        log.info("Deleting health log with ID: {}", id);

        if (!healthLogRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy health log với ID: " + id
            );
        }

        healthLogRepository.deleteById(id);
        log.info("Health log deleted successfully with ID: {}", id);
    }

    @Override
    public void deleteMultipleHealthLogs(List<Long> ids) {
        log.info("Deleting multiple health logs with IDs: {}", ids);

        List<CameraHealthLog> logs = healthLogRepository.findByIdIn(ids);

        if (logs.size() != ids.size()) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Một số health log không tồn tại trong hệ thống"
            );
        }

        healthLogRepository.deleteAllById(ids);
        log.info("Deleted {} health logs successfully", ids.size());
    }

    @Override
    public void deleteHealthLogsByCameraId(Long cameraId) {
        log.info("Deleting all health logs for camera ID: {}", cameraId);

        if (!cameraRepository.existsById(cameraId)) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy camera với ID: " + cameraId
            );
        }

        healthLogRepository.deleteByCameraId(cameraId);
        log.info("Deleted all health logs for camera ID: {}", cameraId);
    }

    @Override
    public void deleteOldHealthLogs(LocalDateTime beforeDate) {
        log.info("Deleting health logs before: {}", beforeDate);

        healthLogRepository.deleteByCheckedAtBefore(beforeDate);
        log.info("Deleted old health logs before: {}", beforeDate);
    }

    @Override
    @Transactional(readOnly = true)
    public CameraHealthLogResponse getLatestHealthLogByCameraId(Long cameraId) {
        log.info("Getting latest health log for camera ID: {}", cameraId);

        if (!cameraRepository.existsById(cameraId)) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy camera với ID: " + cameraId
            );
        }

        List<CameraHealthLog> logs = healthLogRepository.findLatestByCameraId(cameraId);

        if (logs.isEmpty()) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy health log cho camera ID: " + cameraId
            );
        }

        return healthLogMapper.toResponse(logs.get(0));
    }
}
