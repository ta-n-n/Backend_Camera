package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.ProcessingJob;
import com.example.camerasurveillancesystem.dto.mapper.ProcessingJobMapper;
import com.example.camerasurveillancesystem.dto.request.ai.ProcessingJobCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ai.ProcessingJobResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.ProcessingJobRepository;
import com.example.camerasurveillancesystem.service.ProcessingJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessingJobServiceImpl implements ProcessingJobService {

    private final ProcessingJobRepository jobRepository;
    private final ProcessingJobMapper jobMapper;

    @Override
    @Transactional
    public ProcessingJobResponse createJob(ProcessingJobCreateRequest request) {
        ProcessingJob job = jobMapper.toEntity(request);
        ProcessingJob savedJob = jobRepository.save(job);
        
        log.info("Created processing job {} with type {}", savedJob.getId(), savedJob.getJobType());
        return jobMapper.toResponse(savedJob);
    }

    @Override
    public ProcessingJobResponse getJobById(Long id) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        return jobMapper.toResponse(job);
    }

    @Override
    public List<ProcessingJobResponse> getAllJobs() {
        return jobRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingJobResponse> getJobsByStatus(String status) {
        return jobRepository.findByStatus(status).stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingJobResponse> getJobsByType(String jobType) {
        return jobRepository.findByJobType(jobType).stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingJobResponse> getPendingJobsByPriority() {
        List<String> priorities = List.of("URGENT", "HIGH", "NORMAL", "LOW");
        return priorities.stream()
                .flatMap(priority -> jobRepository.findByStatusAndPriority("PENDING", priority).stream())
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProcessingJobResponse startJob(Long id) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        if (!"PENDING".equals(job.getStatus())) {
            throw new IllegalStateException("Only PENDING jobs can be started");
        }

        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        job.setProgress(0);

        ProcessingJob updatedJob = jobRepository.save(job);
        log.info("Started processing job {}", id);
        
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public ProcessingJobResponse completeJob(Long id, String result) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        job.setStatus("COMPLETED");
        job.setResult(result);
        job.setProgress(100);
        job.setCompletedAt(LocalDateTime.now());

        ProcessingJob updatedJob = jobRepository.save(job);
        log.info("Completed processing job {}", id);
        
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public ProcessingJobResponse failJob(Long id, String errorMessage) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        job.setStatus("FAILED");
        job.setErrorMessage(errorMessage);
        job.setCompletedAt(LocalDateTime.now());

        ProcessingJob updatedJob = jobRepository.save(job);
        log.error("Failed processing job {}: {}", id, errorMessage);
        
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public ProcessingJobResponse cancelJob(Long id) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        if ("COMPLETED".equals(job.getStatus())) {
            throw new IllegalStateException("Cannot cancel completed job");
        }

        job.setStatus("CANCELLED");
        job.setCompletedAt(LocalDateTime.now());

        ProcessingJob updatedJob = jobRepository.save(job);
        log.info("Cancelled processing job {}", id);
        
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public ProcessingJobResponse retryJob(Long id) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        if (!"FAILED".equals(job.getStatus())) {
            throw new IllegalStateException("Only FAILED jobs can be retried");
        }

        if (job.getRetryCount() >= job.getMaxRetries()) {
            throw new IllegalStateException("Maximum retry attempts reached");
        }

        job.setStatus("PENDING");
        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(null);
        job.setStartedAt(null);
        job.setCompletedAt(null);
        job.setProgress(0);

        ProcessingJob updatedJob = jobRepository.save(job);
        log.info("Retrying processing job {} (attempt {})", id, updatedJob.getRetryCount());
        
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public ProcessingJobResponse updateProgress(Long id, Integer progress) {
        ProcessingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }

        job.setProgress(progress);
        ProcessingJob updatedJob = jobRepository.save(job);
        
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public long countJobsByStatus(String status) {
        return jobRepository.countByStatus(status);
    }

    @Override
    @Transactional
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND);
        }

        jobRepository.deleteById(id);
        log.info("Deleted processing job {}", id);
    }
}
