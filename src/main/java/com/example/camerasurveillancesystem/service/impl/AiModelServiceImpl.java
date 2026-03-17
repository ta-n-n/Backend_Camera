package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.AiModel;
import com.example.camerasurveillancesystem.dto.mapper.AiModelMapper;
import com.example.camerasurveillancesystem.dto.request.ai.AiModelCreateRequest;
import com.example.camerasurveillancesystem.dto.request.ai.AiModelUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ai.AiModelResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.AiModelRepository;
import com.example.camerasurveillancesystem.service.AiModelService;
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
public class AiModelServiceImpl implements AiModelService {

    private final AiModelRepository modelRepository;
    private final AiModelMapper modelMapper;

    @Override
    @Transactional
    public AiModelResponse createModel(AiModelCreateRequest request) {
        // Check if model with same name and version already exists
        if (modelRepository.existsByNameAndVersion(request.getName(), request.getVersion())) {
            throw new IllegalArgumentException("Model with name '" + request.getName() + 
                    "' and version '" + request.getVersion() + "' already exists");
        }

        AiModel model = modelMapper.toEntity(request);
        AiModel savedModel = modelRepository.save(model);
        
        log.info("Created AI model {} v{}", savedModel.getName(), savedModel.getVersion());
        return modelMapper.toResponse(savedModel);
    }

    @Override
    @Transactional
    public AiModelResponse updateModel(Long id, AiModelUpdateRequest request) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        if (request.getName() != null) {
            model.setName(request.getName());
        }
        if (request.getVersion() != null) {
            model.setVersion(request.getVersion());
        }
        if (request.getType() != null) {
            model.setType(request.getType());
        }
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }
        if (request.getModelPath() != null) {
            model.setModelPath(request.getModelPath());
        }
        if (request.getConfigPath() != null) {
            model.setConfigPath(request.getConfigPath());
        }
        if (request.getAccuracy() != null) {
            model.setAccuracy(request.getAccuracy());
        }
        if (request.getIsActive() != null) {
            model.setIsActive(request.getIsActive());
        }

        AiModel updatedModel = modelRepository.save(model);
        log.info("Updated AI model {}", id);
        
        return modelMapper.toResponse(updatedModel);
    }

    @Override
    public AiModelResponse getModelById(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
        return modelMapper.toResponse(model);
    }

    @Override
    public List<AiModelResponse> getAllModels() {
        return modelRepository.findAll().stream()
                .map(modelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiModelResponse> getActiveModels() {
        return modelRepository.findByIsActive(true).stream()
                .map(modelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiModelResponse> getModelsByType(String type) {
        return modelRepository.findByType(type).stream()
                .map(modelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AiModelResponse toggleModelStatus(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));

        model.setIsActive(!model.getIsActive());
        AiModel updatedModel = modelRepository.save(model);
        
        log.info("Toggled AI model {} status to {}", id, updatedModel.getIsActive());
        return modelMapper.toResponse(updatedModel);
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        if (!modelRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND);
        }

        modelRepository.deleteById(id);
        log.info("Deleted AI model {}", id);
    }

    @Override
    public boolean existsByNameAndVersion(String name, String version) {
        return modelRepository.existsByNameAndVersion(name, version);
    }
}
