package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.SnapshotImage;
import com.example.camerasurveillancesystem.dto.mapper.SnapshotImageMapper;
import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageCreateRequest;
import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageSearchRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.media.SnapshotImageResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.repository.SnapshotImageRepository;
import com.example.camerasurveillancesystem.service.SnapshotImageService;
import com.example.camerasurveillancesystem.specification.SnapshotImageSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnapshotImageServiceImpl implements SnapshotImageService {

    private final SnapshotImageRepository snapshotRepository;
    private final CameraRepository cameraRepository;
    private final SnapshotImageMapper snapshotMapper;
    
    private static final String UPLOAD_DIR = "uploads/snapshots/";

    @Override
    @Transactional
    @CacheEvict(value = "snapshots", allEntries = true)
    public SnapshotImageResponse createSnapshot(SnapshotImageCreateRequest request) {
        SnapshotImage snapshot = snapshotMapper.toEntity(request);
        SnapshotImage savedSnapshot = snapshotRepository.save(snapshot);
        log.info("Created snapshot {}", savedSnapshot.getId());
        return snapshotMapper.toResponse(savedSnapshot);
    }

    @Override
    @Transactional
    @CacheEvict(value = "snapshots", allEntries = true)
    public SnapshotImageResponse captureSnapshot(Long cameraId, String description) {
        Camera camera = cameraRepository.findById(cameraId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        
        SnapshotImage snapshot = new SnapshotImage();
        snapshot.setCamera(camera);
        snapshot.setCaptureType("MANUAL");
        snapshot.setFormat("JPEG");
        snapshot.setFilePath(UPLOAD_DIR + "snapshot_" + UUID.randomUUID() + ".jpg");
        snapshot.setFileName("snapshot_" + System.currentTimeMillis() + ".jpg");
        snapshot.setDescription(description);
        
        SnapshotImage savedSnapshot = snapshotRepository.save(snapshot);
        log.info("Captured snapshot {} for camera {}", savedSnapshot.getId(), cameraId);
        
        return snapshotMapper.toResponse(savedSnapshot);
    }

    @Override
    @Transactional
    @CacheEvict(value = "snapshots", allEntries = true)
    public SnapshotImageResponse uploadSnapshot(Long cameraId, MultipartFile file, String description, String triggerType) {
        Camera camera = cameraRepository.findById(cameraId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        
        try {
            // Create directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = "snapshot_" + UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(filename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create snapshot entity
            SnapshotImage snapshot = new SnapshotImage();
            snapshot.setCamera(camera);
            snapshot.setFilePath(filePath.toString());
            snapshot.setFileName(filename);
            snapshot.setFileSize(file.getSize());
            snapshot.setFormat(extension.replace(".", "").toUpperCase());
            snapshot.setDescription(description);
            snapshot.setCaptureType(triggerType != null ? triggerType : "MANUAL");
            
            SnapshotImage savedSnapshot = snapshotRepository.save(snapshot);
            log.info("Uploaded snapshot {} for camera {}", savedSnapshot.getId(), cameraId);
            
            return snapshotMapper.toResponse(savedSnapshot);
        } catch (Exception e) {
            log.error("Failed to upload snapshot: {}", e.getMessage());
            throw new RuntimeException("Failed to upload snapshot", e);
        }
    }

    @Override
    @Cacheable(value = "snapshots", key = "#id")
    public SnapshotImageResponse getSnapshotById(Long id) {
        SnapshotImage snapshot = snapshotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        return snapshotMapper.toResponse(snapshot);
    }

    @Override
    public PageResponse<SnapshotImageResponse> searchSnapshots(SnapshotImageSearchRequest request) {
        Specification<SnapshotImage> spec = Specification.where(null);
        
        if (request.getCameraId() != null) {
            spec = spec.and(SnapshotImageSpecification.hasCameraId(request.getCameraId()));
        }
        
        if (request.getTriggerType() != null) {
            spec = spec.and(SnapshotImageSpecification.hasCaptureType(request.getTriggerType()));
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            spec = spec.and(SnapshotImageSpecification.capturedBetween(request.getStartDate(), request.getEndDate()));
        }
        
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "capturedAt"));
        var snapshotPage = snapshotRepository.findAll(spec, pageable);
        
        return PageResponse.<SnapshotImageResponse>builder()
            .content(snapshotPage.getContent().stream()
                .map(snapshotMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(snapshotPage.getNumber())
            .pageSize(snapshotPage.getSize())
            .totalElements(snapshotPage.getTotalElements())
            .totalPages(snapshotPage.getTotalPages())
            .last(snapshotPage.isLast())
            .build();
    }

    @Override
    public PageResponse<SnapshotImageResponse> getSnapshotsByCamera(Long cameraId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "capturedAt"));
        var snapshotPage = snapshotRepository.findByCameraId(cameraId, pageable);
        
        return PageResponse.<SnapshotImageResponse>builder()
            .content(snapshotPage.getContent().stream()
                .map(snapshotMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(snapshotPage.getNumber())
            .pageSize(snapshotPage.getSize())
            .totalElements(snapshotPage.getTotalElements())
            .totalPages(snapshotPage.getTotalPages())
            .last(snapshotPage.isLast())
            .build();
    }

    @Override
    public PageResponse<SnapshotImageResponse> getSnapshotsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size) {
        Specification<SnapshotImage> spec = SnapshotImageSpecification.capturedBetween(startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "capturedAt"));
        var snapshotPage = snapshotRepository.findAll(spec, pageable);
        
        return PageResponse.<SnapshotImageResponse>builder()
            .content(snapshotPage.getContent().stream()
                .map(snapshotMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(snapshotPage.getNumber())
            .pageSize(snapshotPage.getSize())
            .totalElements(snapshotPage.getTotalElements())
            .totalPages(snapshotPage.getTotalPages())
            .last(snapshotPage.isLast())
            .build();
    }

    @Override
    public List<SnapshotImageResponse> getSnapshotsByEventId(Long eventId) {
        List<SnapshotImage> snapshots = snapshotRepository.findByAiEventId(eventId);
        return snapshots.stream()
            .map(snapshotMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<SnapshotImageResponse> getSnapshotsByAlertId(Long alertId) {
        List<SnapshotImage> snapshots = snapshotRepository.findByAlertId(alertId);
        return snapshots.stream()
            .map(snapshotMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public SnapshotImageResponse getLatestSnapshot(Long cameraId) {
        return snapshotRepository.findLatestByCameraId(cameraId)
            .map(snapshotMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
    }

    @Override
    public Resource viewImage(Long id) {
        SnapshotImage snapshot = snapshotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        
        try {
            Path filePath = Paths.get(snapshot.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read snapshot file: " + snapshot.getFilePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading snapshot file", e);
        }
    }

    @Override
    public Resource downloadImage(Long id) {
        return viewImage(id); // Same implementation
    }

    @Override
    public Resource getThumbnail(Long id, Integer width, Integer height) {
        // TODO: Implement thumbnail generation with dimensions
        // For now, return the original image
        return viewImage(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "snapshots", allEntries = true)
    public void deleteSnapshot(Long id) {
        SnapshotImage snapshot = snapshotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        
        try {
            // Delete physical file
            Path filePath = Paths.get(snapshot.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Delete from database
            snapshotRepository.delete(snapshot);
            log.info("Deleted snapshot {}", id);
        } catch (Exception e) {
            log.error("Failed to delete snapshot {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete snapshot", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "snapshots", allEntries = true)
    public int cleanupOldSnapshots(Integer daysToKeep) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
        Pageable pageable = PageRequest.of(0, 1000);
        
        List<SnapshotImage> oldSnapshots = snapshotRepository.findOldSnapshotsForCleanup(threshold, pageable);
        
        int deletedCount = 0;
        for (SnapshotImage snapshot : oldSnapshots) {
            try {
                // Delete physical file
                Path filePath = Paths.get(snapshot.getFilePath());
                Files.deleteIfExists(filePath);
                
                // Delete from database
                snapshotRepository.delete(snapshot);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete snapshot {}: {}", snapshot.getId(), e.getMessage());
            }
        }
        
        log.info("Cleaned up {} old snapshots", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @CacheEvict(value = "snapshots", allEntries = true)
    public void deleteSnapshotsByCamera(Long cameraId) {
        List<SnapshotImage> snapshots = snapshotRepository.findByCameraId(cameraId, Pageable.unpaged()).getContent();
        
        for (SnapshotImage snapshot : snapshots) {
            try {
                // Delete physical file
                Path filePath = Paths.get(snapshot.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                log.error("Failed to delete snapshot file {}: {}", snapshot.getFilePath(), e.getMessage());
            }
        }
        
        snapshotRepository.deleteAll(snapshots);
        log.info("Deleted {} snapshots for camera {}", snapshots.size(), cameraId);
    }
}
