package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.AiEventObject;
import com.example.camerasurveillancesystem.dto.response.AiEventObjectResponse;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.repository.AiEventObjectRepository;
import com.example.camerasurveillancesystem.service.AiEventObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiEventObjectServiceImpl implements AiEventObjectService {

    private final AiEventObjectRepository objectRepository;

    @Override
    public List<AiEventObjectResponse> getObjectsByEventId(Long aiEventId) {
        return objectRepository.findByAiEventId(aiEventId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AiEventObjectResponse getObjectById(Long id) {
        AiEventObject object = objectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AI_EVENT_NOT_FOUND));
        return convertToResponse(object);
    }

    @Override
    public List<AiEventObjectResponse> getObjectsByType(String objectType) {
        return objectRepository.findByObjectType(objectType).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventObjectResponse> getObjectsByMinConfidence(Double minConfidence) {
        return objectRepository.findByConfidenceGreaterThanEqual(minConfidence).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiEventObjectResponse> getObjectsByEventAndType(Long aiEventId, String objectType) {
        return objectRepository.findByAiEventIdAndObjectType(aiEventId, objectType).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countObjectsByEvent(Long aiEventId) {
        return objectRepository.countByAiEventId(aiEventId);
    }

    @Override
    public List<String> getAllObjectTypes() {
        return objectRepository.findDistinctObjectTypes();
    }

    @Override
    @Transactional
    public void deleteObject(Long id) {
        if (!objectRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.AI_EVENT_NOT_FOUND);
        }
        objectRepository.deleteById(id);
    }

    private AiEventObjectResponse convertToResponse(AiEventObject object) {
        return AiEventObjectResponse.builder()
                .id(object.getId())
                .aiEventId(object.getAiEvent().getId())
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
