package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    /**
     * Get all permissions
     */
    List<PermissionResponse> getAllPermissions();

    /**
     * Get permission by ID
     */
    PermissionResponse getPermissionById(Long id);

    /**
     * Create new permission
     */
    PermissionResponse createPermission(String name, String description);

    /**
     * Update permission
     */
    PermissionResponse updatePermission(Long id, String name, String description);

    /**
     * Delete permission
     */
    void deletePermission(Long id);
}
