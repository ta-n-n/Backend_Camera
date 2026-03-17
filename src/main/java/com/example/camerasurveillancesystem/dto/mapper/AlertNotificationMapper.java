package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.AlertNotification;
import com.example.camerasurveillancesystem.dto.request.alert.AlertNotificationCreateRequest;
import com.example.camerasurveillancesystem.dto.response.alert.AlertNotificationResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlertNotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alert", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(source = "recipient", target = "recipientAddress")
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "retryCount", constant = "0")
    AlertNotification toEntity(AlertNotificationCreateRequest request);

    @Mapping(source = "alert.id", target = "alertId")
    @Mapping(source = "channel.id", target = "notificationChannelId")
    @Mapping(source = "channel.name", target = "channelName")
    @Mapping(source = "channel.channelType", target = "channelType")
    @Mapping(source = "recipientAddress", target = "recipient")
    @Mapping(target = "metadata", ignore = true)
    AlertNotificationResponse toResponse(AlertNotification alertNotification);
}
