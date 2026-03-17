package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.ai.AiModelCreateRequest;
import com.example.camerasurveillancesystem.dto.request.ai.AiModelUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.ai.AiModelResponse;
import com.example.camerasurveillancesystem.service.AiModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-models")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService modelService;

    /**
     * Create new AI model
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AiModelResponse>> createModel(
            @Valid @RequestBody AiModelCreateRequest request) {
        AiModelResponse model = modelService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("AI model created successfully", model));
    }

    /**
     * Update AI model
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiModelResponse>> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody AiModelUpdateRequest request) {
        AiModelResponse model = modelService.updateModel(id, request);
        return ResponseEntity.ok(ApiResponse.success("AI model updated successfully", model));
    }

    /**
     * Get model by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiModelResponse>> getModelById(@PathVariable Long id) {
        AiModelResponse model = modelService.getModelById(id);
        return ResponseEntity.ok(ApiResponse.success(model));
    }

    /**
     * Get all models
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AiModelResponse>>> getAllModels() {
        List<AiModelResponse> models = modelService.getAllModels();
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    /**
     * Get active models
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AiModelResponse>>> getActiveModels() {
        List<AiModelResponse> models = modelService.getActiveModels();
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    /**
     * Get models by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<AiModelResponse>>> getModelsByType(@PathVariable String type) {
        List<AiModelResponse> models = modelService.getModelsByType(type);
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    /**
     * Toggle model active status
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<AiModelResponse>> toggleModelStatus(@PathVariable Long id) {
        AiModelResponse model = modelService.toggleModelStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Model status toggled successfully", model));
    }

    /**
     * Delete model
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id);
        return ResponseEntity.ok(ApiResponse.success("AI model deleted successfully", null));
    }

    /**
     * Check if model exists by name and version
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkModelExists(
            @RequestParam String name,
            @RequestParam String version) {
        boolean exists = modelService.existsByNameAndVersion(name, version);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
