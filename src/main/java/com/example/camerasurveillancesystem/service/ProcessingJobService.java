package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.ai.ProcessingJobCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ai.ProcessingJobResponse;

import java.util.List;

public interface ProcessingJobService {

    /**
     * Create new processing job
     */
    ProcessingJobResponse createJob(ProcessingJobCreateRequest request);

    /**
     * Get job by ID
     */
    ProcessingJobResponse getJobById(Long id);

    /**
     * Get all jobs
     */
    List<ProcessingJobResponse> getAllJobs();

    /**
     * Get jobs by status
     */
    List<ProcessingJobResponse> getJobsByStatus(String status);

    /**
     * Get jobs by type
     */
    List<ProcessingJobResponse> getJobsByType(String jobType);

    /**
     * Get pending jobs ordered by priority
     */
    List<ProcessingJobResponse> getPendingJobsByPriority();

    /**
     * Start job execution
     */
    ProcessingJobResponse startJob(Long id);

    /**
     * Complete job with result
     */
    ProcessingJobResponse completeJob(Long id, String result);

    /**
     * Fail job with error message
     */
    ProcessingJobResponse failJob(Long id, String errorMessage);

    /**
     * Cancel job
     */
    ProcessingJobResponse cancelJob(Long id);

    /**
     * Retry failed job
     */
    ProcessingJobResponse retryJob(Long id);

    /**
     * Update job progress
     */
    ProcessingJobResponse updateProgress(Long id, Integer progress);

    /**
     * Count jobs by status
     */
    long countJobsByStatus(String status);

    /**
     * Delete job
     */
    void deleteJob(Long id);
}
