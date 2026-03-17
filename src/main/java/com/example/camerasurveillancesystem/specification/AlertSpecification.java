package com.example.camerasurveillancesystem.specification;

import com.example.camerasurveillancesystem.domain.AiEvent;
import com.example.camerasurveillancesystem.domain.Alert;
import com.example.camerasurveillancesystem.domain.Camera;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AlertSpecification {

    /**
     * Filter by camera ID (thong qua aiEvent)
     */
    public static Specification<Alert> hasCameraId(Long cameraId) {
        return (root, query, criteriaBuilder) -> {
            if (cameraId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Alert, AiEvent> aiEventJoin = root.join("aiEvent");
            Join<AiEvent, Camera> cameraJoin = aiEventJoin.join("camera");
            return criteriaBuilder.equal(cameraJoin.get("id"), cameraId);
        };
    }

    /**
     * Filter by alert type
     */
    public static Specification<Alert> hasAlertType(String alertType) {
        return (root, query, criteriaBuilder) -> {
            if (alertType == null || alertType.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("alertType"), alertType);
        };
    }

    /**
     * Filter by severity
     */
    public static Specification<Alert> hasSeverity(String severity) {
        return (root, query, criteriaBuilder) -> {
            if (severity == null || severity.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("severity"), severity);
        };
    }

    /**
     * Filter by status
     */
    public static Specification<Alert> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by assigned user
     */
    public static Specification<Alert> isAssignedToUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("assignedTo").get("id"), userId);
        };
    }

    /**
     * Filter by date range
     */
    public static Specification<Alert> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
        };
    }

    /**
     * Filter unresolved alerts (PENDING, NEW or ACKNOWLEDGED)
     */
    public static Specification<Alert> isUnresolved() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.or(
                criteriaBuilder.equal(root.get("status"), "PENDING"),
                criteriaBuilder.equal(root.get("status"), "NEW"),
                criteriaBuilder.equal(root.get("status"), "ACKNOWLEDGED")
            );
    }

    /**
     * Filter critical alerts
     */
    public static Specification<Alert> isCritical() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("severity"), "CRITICAL");
    }

    /**
     * Filter unassigned alerts
     */
    public static Specification<Alert> isUnassigned() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.isNull(root.get("assignedTo"));
    }
}
