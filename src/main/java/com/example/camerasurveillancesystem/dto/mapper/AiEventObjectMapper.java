package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.AiEventObject;
import com.example.camerasurveillancesystem.dto.response.ai.AiEventObjectResponse;
import org.springframework.stereotype.Component;

@Component
public class AiEventObjectMapper {

    public AiEventObjectResponse toResponse(AiEventObject object) {
        if (object == null) {
            return null;
        }

        return AiEventObjectResponse.builder()
                .id(object.getId())
                .aiEventId(object.getAiEvent() != null ? object.getAiEvent().getId() : null)
                .objectType(object.getObjectType())
                .confidence(object.getConfidence())
                .label(object.getLabel())
                .boundingBoxX(object.getBoundingBoxX())
                .boundingBoxY(object.getBoundingBoxY())
                .boundingBoxWidth(object.getBoundingBoxWidth())
                .boundingBoxHeight(object.getBoundingBoxHeight())
                .attributes(object.getAttributes())
                .createdAt(object.getCreatedAt())
                .build();
    }
}
