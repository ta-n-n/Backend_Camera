package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.ai.AiModelCreateRequest;
import com.example.camerasurveillancesystem.dto.request.ai.AiModelUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ai.AiModelResponse;

import java.util.List;

public interface AiModelService {

    /**
     * Create new AI model
     */
    AiModelResponse createModel(AiModelCreateRequest request);

    /**
     * Update AI model
     */
    AiModelResponse updateModel(Long id, AiModelUpdateRequest request);

    /**
     * Get model by ID
     */
    AiModelResponse getModelById(Long id);

    /**
     * Get all models
     */
    List<AiModelResponse> getAllModels();

    /**
     * Get active models
     */
    List<AiModelResponse> getActiveModels();

    /**
     * Get models by type
     */
    List<AiModelResponse> getModelsByType(String type);

    /**
     * Toggle model active status
     */
    AiModelResponse toggleModelStatus(Long id);

    /**
     * Delete model
     */
    void deleteModel(Long id);

    /**
     * Check if model exists by name and version
     */
    boolean existsByNameAndVersion(String name, String version);
}
