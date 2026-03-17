package com.example.camerasurveillancesystem.specification;

import com.example.camerasurveillancesystem.domain.SnapshotImage;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SnapshotImageSpecification {

    /**
     * Filter by camera ID
     */
    public static Specification<SnapshotImage> hasCameraId(Long cameraId) {
        return (root, query, criteriaBuilder) -> {
            if (cameraId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("camera").get("id"), cameraId);
        };
    }

    /**
     * Filter by capture type
     */
    public static Specification<SnapshotImage> hasCaptureType(String captureType) {
        return (root, query, criteriaBuilder) -> {
            if (captureType == null || captureType.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("captureType"), captureType);
        };
    }

    /**
     * Filter by date range
     */
    public static Specification<SnapshotImage> capturedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("capturedAt"), startDate, endDate);
        };
    }

    /**
     * Filter by format
     */
    public static Specification<SnapshotImage> hasFormat(String format) {
        return (root, query, criteriaBuilder) -> {
            if (format == null || format.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("format"), format);
        };
    }

    /**
     * Filter by resolution
     */
    public static Specification<SnapshotImage> hasResolution(String resolution) {
        return (root, query, criteriaBuilder) -> {
            if (resolution == null || resolution.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("resolution"), resolution);
        };
    }
}
