package com.example.camerasurveillancesystem.dto.mapper;

import com.example.camerasurveillancesystem.domain.AiEvent;
import com.example.camerasurveillancesystem.dto.response.ai.AiEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AiEventMapper {

    private final AiEventObjectMapper objectMapper;

    public AiEventResponse toResponse(AiEvent event) {
        if (event == null) {
            return null;
        }

        return AiEventResponse.builder()
                .id(event.getId())
                .cameraId(event.getCamera() != null ? event.getCamera().getId() : null)
                .cameraName(event.getCamera() != null ? event.getCamera().getName() : null)
                .modelId(event.getModel() != null ? event.getModel().getId() : null)
                .modelName(event.getModel() != null ? event.getModel().getName() : null)
                .eventType(event.getEventType())
                .confidenceScore(event.getConfidenceScore())
                .snapshotPath(event.getSnapshotPath())
                .metadata(event.getMetadata())
                .detectedAt(event.getDetectedAt())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .detectedObjects(event.getDetectedObjects() != null ?
                        event.getDetectedObjects().stream()
                                .map(objectMapper::toResponse)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
