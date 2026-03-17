package com.example.camerasurveillancesystem.specification;

import com.example.camerasurveillancesystem.domain.CameraHealthLog;
import com.example.camerasurveillancesystem.dto.request.CameraHealthLogSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CameraHealthLogSpecification {

    public static Specification<CameraHealthLog> withFilters(CameraHealthLogSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by camera
            if (request.getCameraId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("camera").get("id"), request.getCameraId()));
            }

            // Filter by status
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            // Filter by date range
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(
                    root.get("checkedAt"),
                    request.getStartDate(),
                    request.getEndDate()
                ));
            } else if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("checkedAt"),
                    request.getStartDate()
                ));
            } else if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("checkedAt"),
                    request.getEndDate()
                ));
            }

            // Filter by error code
            if (request.getErrorCode() != null && !request.getErrorCode().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("errorCode"), request.getErrorCode()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
