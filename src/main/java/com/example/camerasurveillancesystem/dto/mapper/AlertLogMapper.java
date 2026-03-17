package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.AlertLog;
import com.example.camerasurveillancesystem.dto.request.alert.AlertLogCreateRequest;
import com.example.camerasurveillancesystem.dto.response.alert.AlertLogResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlertLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alert", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "actionType", target = "action")
    @Mapping(source = "comment", target = "comment")
    AlertLog toEntity(AlertLogCreateRequest request);

    @Mapping(source = "alert.id", target = "alertId")
    @Mapping(source = "user.id", target = "performedById")
    @Mapping(source = "user.username", target = "performedByName")
    @Mapping(source = "action", target = "actionType")
    @Mapping(source = "previousStatus", target = "previousValue")
    @Mapping(source = "newStatus", target = "newValue")
    AlertLogResponse toResponse(AlertLog alertLog);
}
