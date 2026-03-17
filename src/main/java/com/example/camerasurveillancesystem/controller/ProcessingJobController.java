package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.ai.ProcessingJobCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.ai.ProcessingJobResponse;
import com.example.camerasurveillancesystem.service.ProcessingJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/processing-jobs")
@RequiredArgsConstructor
public class ProcessingJobController {

    private final ProcessingJobService jobService;

    /**
     * Create new processing job
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> createJob(
            @Valid @RequestBody ProcessingJobCreateRequest request) {
        ProcessingJobResponse job = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Processing job created successfully", job));
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> getJobById(@PathVariable Long id) {
        ProcessingJobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    /**
     * Get all jobs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcessingJobResponse>>> getAllJobs() {
        List<ProcessingJobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    /**
     * Get jobs by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProcessingJobResponse>>> getJobsByStatus(@PathVariable String status) {
        List<ProcessingJobResponse> jobs = jobService.getJobsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    /**
     * Get jobs by type
     */
    @GetMapping("/type/{jobType}")
    public ResponseEntity<ApiResponse<List<ProcessingJobResponse>>> getJobsByType(@PathVariable String jobType) {
        List<ProcessingJobResponse> jobs = jobService.getJobsByType(jobType);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    /**
     * Get pending jobs ordered by priority
     */
    @GetMapping("/pending/prioritized")
    public ResponseEntity<ApiResponse<List<ProcessingJobResponse>>> getPendingJobsByPriority() {
        List<ProcessingJobResponse> jobs = jobService.getPendingJobsByPriority();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    /**
     * Start job execution
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> startJob(@PathVariable Long id) {
        ProcessingJobResponse job = jobService.startJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job started successfully", job));
    }

    /**
     * Complete job with result
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> completeJob(
            @PathVariable Long id,
            @RequestBody String result) {
        ProcessingJobResponse job = jobService.completeJob(id, result);
        return ResponseEntity.ok(ApiResponse.success("Job completed successfully", job));
    }

    /**
     * Fail job with error message
     */
    @PostMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> failJob(
            @PathVariable Long id,
            @RequestBody String errorMessage) {
        ProcessingJobResponse job = jobService.failJob(id, errorMessage);
        return ResponseEntity.ok(ApiResponse.success("Job marked as failed", job));
    }

    /**
     * Cancel job
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> cancelJob(@PathVariable Long id) {
        ProcessingJobResponse job = jobService.cancelJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job cancelled successfully", job));
    }

    /**
     * Retry failed job
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> retryJob(@PathVariable Long id) {
        ProcessingJobResponse job = jobService.retryJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job queued for retry", job));
    }

    /**
     * Update job progress
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer progress) {
        ProcessingJobResponse job = jobService.updateProgress(id, progress);
        return ResponseEntity.ok(ApiResponse.success("Job progress updated", job));
    }

    /**
     * Count jobs by status
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<ApiResponse<Long>> countJobsByStatus(@PathVariable String status) {
        long count = jobService.countJobsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Delete job
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Processing job deleted successfully", null));
    }
}
