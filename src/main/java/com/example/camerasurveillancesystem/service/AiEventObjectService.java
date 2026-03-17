package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.AiEventObjectResponse;

import java.util.List;

public interface AiEventObjectService {
    
    /**
     * Get all objects for specific AI event
     */
    List<AiEventObjectResponse> getObjectsByEventId(Long aiEventId);
    
    /**
     * Get object by ID
     */
    AiEventObjectResponse getObjectById(Long id);
    
    /**
     * Get objects by type
     */
    List<AiEventObjectResponse> getObjectsByType(String objectType);
    
    /**
     * Get objects with confidence above threshold
     */
    List<AiEventObjectResponse> getObjectsByMinConfidence(Double minConfidence);
    
    /**
     * Get objects by event and type
     */
    List<AiEventObjectResponse> getObjectsByEventAndType(Long aiEventId, String objectType);
    
    /**
     * Get object count for event
     */
    long countObjectsByEvent(Long aiEventId);
    
    /**
     * Get all distinct object types
     */
    List<String> getAllObjectTypes();
    
    /**
     * Delete object
     */
    void deleteObject(Long id);
}
