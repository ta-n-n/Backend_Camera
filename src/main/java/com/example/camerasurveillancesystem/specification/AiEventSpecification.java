package com.example.camerasurveillancesystem.specification;

import com.example.camerasurveillancesystem.domain.AiEvent;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AiEventSpecification {

    /**
     * Filter by camera ID
     */
    public static Specification<AiEvent> hasCameraId(Long cameraId) {
        return (root, query, cb) -> {
            if (cameraId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("camera").get("id"), cameraId);
        };
    }

    /**
     * Filter by AI model ID
     */
    public static Specification<AiEvent> hasModelId(Long modelId) {
        return (root, query, cb) -> {
            if (modelId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("model").get("id"), modelId);
        };
    }

    /**
     * Filter by event type
     */
    public static Specification<AiEvent> hasEventType(String eventType) {
        return (root, query, cb) -> {
            if (eventType == null || eventType.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("eventType"), eventType);
        };
    }

    /**
     * Filter by minimum confidence score
     */
    public static Specification<AiEvent> hasMinConfidence(Double minConfidence) {
        return (root, query, cb) -> {
            if (minConfidence == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("confidenceScore"), minConfidence);
        };
    }

    /**
     * Filter by detected date range
     */
    public static Specification<AiEvent> detectedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) {
                return cb.conjunction();
            }
            return cb.between(root.get("detectedAt"), startDate, endDate);
        };
    }

    /**
     * Filter events that have alerts
     */
    public static Specification<AiEvent> hasAlerts() {
        return (root, query, cb) -> {
            var alertsJoin = root.join("alerts", JoinType.INNER);
            return cb.isNotNull(alertsJoin.get("id"));
        };
    }

    /**
     * Filter events by specific object type detected
     */
    public static Specification<AiEvent> hasObjectType(String objectType) {
        return (root, query, cb) -> {
            if (objectType == null || objectType.isBlank()) {
                return cb.conjunction();
            }
            var objectsJoin = root.join("detectedObjects", JoinType.INNER);
            return cb.equal(objectsJoin.get("objectType"), objectType);
        };
    }

    /**
     * Filter recent events (last N hours)
     */
    public static Specification<AiEvent> detectedInLastHours(int hours) {
        return (root, query, cb) -> {
            LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
            return cb.greaterThanOrEqualTo(root.get("detectedAt"), threshold);
        };
    }
}
