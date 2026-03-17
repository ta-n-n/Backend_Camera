package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.NotificationChannel;
import com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelCreateRequest;
import com.example.camerasurveillancesystem.dto.response.alert.NotificationChannelResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationChannelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    NotificationChannel toEntity(NotificationChannelCreateRequest request);

    NotificationChannelResponse toResponse(NotificationChannel notificationChannel);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntity(@MappingTarget NotificationChannel notificationChannel, 
                     com.example.camerasurveillancesystem.dto.request.alert.NotificationChannelUpdateRequest request);
}
