package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.RoleResponse;

import java.util.List;
import java.util.Set;

public interface RoleService {

    /**
     * Get all roles
     */
    List<RoleResponse> getAllRoles();

    /**
     * Get role by ID
     */
    RoleResponse getRoleById(Long id);

    /**
     * Create new role
     */
    RoleResponse createRole(String name, String description, Set<Long> permissionIds);

    /**
     * Update role
     */
    RoleResponse updateRole(Long id, String name, String description, Set<Long> permissionIds);

    /**
     * Delete role
     */
    void deleteRole(Long id);

    /**
     * Add permission to role
     */
    void addPermissionToRole(Long roleId, Long permissionId);

    /**
     * Remove permission from role
     */
    void removePermissionFromRole(Long roleId, Long permissionId);

    /**
     * Update role permissions (bulk update)
     */
    RoleResponse updateRolePermissions(Long roleId, Set<Long> permissionIds);
}
