package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.VideoRecord;
import com.example.camerasurveillancesystem.dto.request.media.VideoRecordCreateRequest;
import com.example.camerasurveillancesystem.dto.response.media.VideoRecordResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stream", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VideoRecord toEntity(VideoRecordCreateRequest request);

    @Mapping(source = "stream.camera.id", target = "cameraId")
    @Mapping(source = "stream.camera.name", target = "cameraName")
    @Mapping(target = "aiEventId", ignore = true)
    @Mapping(target = "downloadUrl", ignore = true)
    @Mapping(target = "fileSizeMB", ignore = true)
    VideoRecordResponse toResponse(VideoRecord videoRecord);
}
