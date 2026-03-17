package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.VideoRecord;
import com.example.camerasurveillancesystem.dto.mapper.VideoRecordMapper;
import com.example.camerasurveillancesystem.dto.request.media.VideoRecordCreateRequest;
import com.example.camerasurveillancesystem.dto.request.media.VideoRecordSearchRequest;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.dto.response.media.VideoRecordResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.VideoRecordRepository;
import com.example.camerasurveillancesystem.service.VideoRecordService;
import com.example.camerasurveillancesystem.specification.VideoRecordSpecification;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoRecordServiceImpl implements VideoRecordService {

    private final VideoRecordRepository videoRepository;
    private final VideoRecordMapper videoMapper;

    @Override
    @Transactional
    @CacheEvict(value = "videos", allEntries = true)
    public VideoRecordResponse createVideoRecord(VideoRecordCreateRequest request) {
        VideoRecord video = videoMapper.toEntity(request);
        
        // Calculate duration if not provided
        if (video.getDuration() == null && video.getStartTime() != null && video.getEndTime() != null) {
            long seconds = java.time.Duration.between(video.getStartTime(), video.getEndTime()).getSeconds();
            video.setDuration((int) seconds);
        }
        
        video.setIsArchived(false);
        
        VideoRecord savedVideo = videoRepository.save(video);
        log.info("Created video record {}", savedVideo.getId());
        
        return videoMapper.toResponse(savedVideo);
    }

    @Override
    @Cacheable(value = "videos", key = "#id")
    public VideoRecordResponse getVideoById(Long id) {
        VideoRecord video = videoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        return videoMapper.toResponse(video);
    }

    @Override
    public PageResponse<VideoRecordResponse> searchVideos(VideoRecordSearchRequest request) {
        Specification<VideoRecord> spec = Specification.where(null);
        
        if (request.getCameraId() != null) {
            spec = spec.and(VideoRecordSpecification.hasCameraId(request.getCameraId()));
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            spec = spec.and(VideoRecordSpecification.recordedBetween(request.getStartDate(), request.getEndDate()));
        }
        
        if (request.getMinDuration() != null) {
            spec = spec.and(VideoRecordSpecification.hasMinDuration(request.getMinDuration()));
        }
        
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        var videoPage = videoRepository.findAll(spec, pageable);
        
        return PageResponse.<VideoRecordResponse>builder()
            .content(videoPage.getContent().stream()
                .map(videoMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(videoPage.getNumber())
            .pageSize(videoPage.getSize())
            .totalElements(videoPage.getTotalElements())
            .totalPages(videoPage.getTotalPages())
            .last(videoPage.isLast())
            .build();
    }

    @Override
    public PageResponse<VideoRecordResponse> getVideosByCamera(Long cameraId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        var videoPage = videoRepository.findByCameraId(cameraId, pageable);
        
        return PageResponse.<VideoRecordResponse>builder()
            .content(videoPage.getContent().stream()
                .map(videoMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(videoPage.getNumber())
            .pageSize(videoPage.getSize())
            .totalElements(videoPage.getTotalElements())
            .totalPages(videoPage.getTotalPages())
            .last(videoPage.isLast())
            .build();
    }

    @Override
    public PageResponse<VideoRecordResponse> getVideosByDateRange(LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size) {
        Specification<VideoRecord> spec = VideoRecordSpecification.recordedBetween(startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        var videoPage = videoRepository.findAll(spec, pageable);
        
        return PageResponse.<VideoRecordResponse>builder()
            .content(videoPage.getContent().stream()
                .map(videoMapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(videoPage.getNumber())
            .pageSize(videoPage.getSize())
            .totalElements(videoPage.getTotalElements())
            .totalPages(videoPage.getTotalPages())
            .last(videoPage.isLast())
            .build();
    }

    @Override
    public List<VideoRecordResponse> getVideosByEventId(Long eventId) {
        List<VideoRecord> videos = videoRepository.findByAiEventId(eventId);
        return videos.stream()
            .map(videoMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Resource streamVideo(Long id) {
        VideoRecord video = videoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        
        try {
            Path filePath = Paths.get(video.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read video file: " + video.getFilePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading video file", e);
        }
    }

    @Override
    public Resource downloadVideo(Long id) {
        return streamVideo(id); // Same implementation for now
    }

    @Override
    public com.example.camerasurveillancesystem.dto.response.media.StorageStatistics getStorageStatistics() {
        Long totalVideos = videoRepository.count();
        Long totalSize = videoRepository.getTotalStorageSize();
        
        return com.example.camerasurveillancesystem.dto.response.media.StorageStatistics.builder()
            .totalVideos(totalVideos)
            .totalVideoSizeBytes(totalSize != null ? totalSize : 0L)
            .totalVideoSizeGB(String.format("%.2f", (totalSize != null ? totalSize : 0L) / (1024.0 * 1024.0 * 1024.0)))
            .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "videos", allEntries = true)
    public int cleanupOldVideos(Integer daysToKeep) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
        Pageable pageable = PageRequest.of(0, 1000);
        
        List<VideoRecord> oldVideos = videoRepository.findOldVideosForCleanup(threshold, pageable);
        
        int deletedCount = 0;
        for (VideoRecord video : oldVideos) {
            try {
                videoRepository.delete(video);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete video {}: {}", video.getId(), e.getMessage());
            }
        }
        
        log.info("Cleaned up {} old videos", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @CacheEvict(value = "videos", allEntries = true)
    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND);
        }
        
        videoRepository.deleteById(id);
        log.info("Deleted video {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "videos", allEntries = true)
    public void deleteVideosByCamera(Long cameraId) {
        List<VideoRecord> videos = videoRepository.findByCameraId(cameraId, Pageable.unpaged()).getContent();
        videoRepository.deleteAll(videos);
        log.info("Deleted {} videos for camera {}", videos.size(), cameraId);
    }
}
