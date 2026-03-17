package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.ProcessingJob;
import com.example.camerasurveillancesystem.dto.request.ai.ProcessingJobCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ai.ProcessingJobResponse;
import org.springframework.stereotype.Component;

@Component
public class ProcessingJobMapper {

    public ProcessingJob toEntity(ProcessingJobCreateRequest request) {
        if (request == null) {
            return null;
        }

        ProcessingJob job = new ProcessingJob();
        job.setJobType(request.getJobType());
        job.setPriority(request.getPriority() != null ? request.getPriority() : "NORMAL");
        job.setParameters(request.getParameters());
        job.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3);
        job.setStatus("PENDING");
        job.setProgress(0);
        job.setRetryCount(0);

        return job;
    }

    public ProcessingJobResponse toResponse(ProcessingJob job) {
        if (job == null) {
            return null;
        }

        return ProcessingJobResponse.builder()
                .id(job.getId())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .priority(job.getPriority())
                .parameters(job.getParameters())
                .result(job.getResult())
                .errorMessage(job.getErrorMessage())
                .progress(job.getProgress())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .retryCount(job.getRetryCount())
                .maxRetries(job.getMaxRetries())
                .createdByUserId(job.getCreatedBy() != null ? job.getCreatedBy().getId() : null)
                .createdByUserName(job.getCreatedBy() != null ? job.getCreatedBy().getUsername() : null)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
