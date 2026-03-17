package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.SnapshotImage;
import com.example.camerasurveillancesystem.dto.request.media.SnapshotImageCreateRequest;
import com.example.camerasurveillancesystem.dto.response.media.SnapshotImageResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SnapshotImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "camera", ignore = true)
    @Mapping(target = "capturedAt", ignore = true)
    SnapshotImage toEntity(SnapshotImageCreateRequest request);

    @Mapping(source = "camera.id", target = "cameraId")
    @Mapping(source = "camera.name", target = "cameraName")
    @Mapping(target = "aiEventId", ignore = true)
    @Mapping(target = "alertId", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "fileSizeKB", ignore = true)
    SnapshotImageResponse toResponse(SnapshotImage snapshotImage);
}
