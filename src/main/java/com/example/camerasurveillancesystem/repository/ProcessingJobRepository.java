package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long>, JpaSpecificationExecutor<ProcessingJob> {

    List<ProcessingJob> findByStatus(String status);

    List<ProcessingJob> findByJobType(String jobType);

    List<ProcessingJob> findByStatusAndPriority(String status, String priority);

    List<ProcessingJob> findByCreatedById(Long userId);

    long countByStatus(String status);
}
