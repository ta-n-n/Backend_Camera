package com.example.camerasurveillancesystem.specification;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraStream;
import com.example.camerasurveillancesystem.domain.VideoRecord;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class VideoRecordSpecification {

    /**
     * Filter by camera ID (thông qua stream)
     */
    public static Specification<VideoRecord> hasCameraId(Long cameraId) {
        return (root, query, criteriaBuilder) -> {
            if (cameraId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<VideoRecord, CameraStream> streamJoin = root.join("stream");
            Join<CameraStream, Camera> cameraJoin = streamJoin.join("camera");
            return criteriaBuilder.equal(cameraJoin.get("id"), cameraId);
        };
    }

    /**
     * Filter by stream ID
     */
    public static Specification<VideoRecord> hasStreamId(Long streamId) {
        return (root, query, criteriaBuilder) -> {
            if (streamId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("stream").get("id"), streamId);
        };
    }

    /**
     * Filter by date range (startTime)
     */
    public static Specification<VideoRecord> recordedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("startTime"), startDate, endDate);
        };
    }

    /**
     * Filter by format
     */
    public static Specification<VideoRecord> hasFormat(String format) {
        return (root, query, criteriaBuilder) -> {
            if (format == null || format.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("format"), format);
        };
    }

    /**
     * Filter by minimum duration
     */
    public static Specification<VideoRecord> hasMinDuration(Integer minDuration) {
        return (root, query, criteriaBuilder) -> {
            if (minDuration == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("duration"), minDuration);
        };
    }

    /**
     * Filter archived videos
     */
    public static Specification<VideoRecord> isArchived(Boolean archived) {
        return (root, query, criteriaBuilder) -> {
            if (archived == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isArchived"), archived);
        };
    }

    /**
     * Filter by resolution
     */
    public static Specification<VideoRecord> hasResolution(String resolution) {
        return (root, query, criteriaBuilder) -> {
            if (resolution == null || resolution.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("resolution"), resolution);
        };
    }
}
