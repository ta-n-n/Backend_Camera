package com.example.camerasurveillancesystem.specification;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.dto.request.CameraSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CameraSpecification {

    public static Specification<Camera> withFilters(CameraSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by name
            if (request.getName() != null && !request.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + request.getName().toLowerCase() + "%"
                ));
            }

            // Filter by code
            if (request.getCode() != null && !request.getCode().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + request.getCode().toLowerCase() + "%"
                ));
            }

            // Filter by status
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            // Filter by model
            if (request.getModel() != null && !request.getModel().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("model")),
                        "%" + request.getModel().toLowerCase() + "%"
                ));
            }

            // Filter by manufacturer
            if (request.getManufacturer() != null && !request.getManufacturer().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("manufacturer")),
                        "%" + request.getManufacturer().toLowerCase() + "%"
                ));
            }

            // Filter by location
            if (request.getLocationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("id"), request.getLocationId()));
            }

            // Filter by group
            if (request.getGroupId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("groups").get("id"),
                        request.getGroupId()
                ));
            }

            // Filter by resolution
            if (request.getResolution() != null && !request.getResolution().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("resolution"), request.getResolution()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
