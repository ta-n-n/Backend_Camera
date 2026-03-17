package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.Alert;
import com.example.camerasurveillancesystem.dto.request.alert.AlertCreateRequest;
import com.example.camerasurveillancesystem.dto.response.alert.AlertResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlertMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aiEvent", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "acknowledgedAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "logs", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(source = "snapshotUrl", target = "snapshotPath")
    @Mapping(source = "videoUrl", target = "videoPath")
    Alert toEntity(AlertCreateRequest request);

    @Mapping(source = "aiEvent.id", target = "aiEventId")
    @Mapping(source = "aiEvent.camera.id", target = "cameraId")
    @Mapping(source = "aiEvent.camera.name", target = "cameraName")
    @Mapping(source = "assignedTo.id", target = "assignedToUserId")
    @Mapping(source = "assignedTo.username", target = "assignedToUserName")
    @Mapping(source = "snapshotPath", target = "snapshotUrl")
    @Mapping(source = "videoPath", target = "videoUrl")
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "resolutionNotes", ignore = true)
    AlertResponse toResponse(Alert alert);
}
