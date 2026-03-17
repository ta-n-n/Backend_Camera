package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.AiModel;
import com.example.camerasurveillancesystem.dto.request.ai.AiModelCreateRequest;
import com.example.camerasurveillancesystem.dto.response.ai.AiModelResponse;
import org.springframework.stereotype.Component;

@Component
public class AiModelMapper {

    public AiModel toEntity(AiModelCreateRequest request) {
        if (request == null) {
            return null;
        }

        AiModel model = new AiModel();
        model.setName(request.getName());
        model.setVersion(request.getVersion());
        model.setType(request.getType());
        model.setDescription(request.getDescription());
        model.setModelPath(request.getModelPath());
        model.setConfigPath(request.getConfigPath());
        model.setAccuracy(request.getAccuracy());
        model.setIsActive(request.getIsActive());

        return model;
    }

    public AiModelResponse toResponse(AiModel model) {
        if (model == null) {
            return null;
        }

        return AiModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .version(model.getVersion())
                .type(model.getType())
                .description(model.getDescription())
                .modelPath(model.getModelPath())
                .configPath(model.getConfigPath())
                .accuracy(model.getAccuracy())
                .isActive(model.getIsActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
