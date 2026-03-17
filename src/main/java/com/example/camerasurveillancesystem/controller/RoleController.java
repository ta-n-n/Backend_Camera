package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.UpdateRolePermissionsRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.RoleResponse;
import com.example.camerasurveillancesystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all roles
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Create new role
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Set<Long> permissionIds
    ) {
        RoleResponse response = roleService.createRole(name, description, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", response));
    }

    /**
     * Update role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Set<Long> permissionIds
    ) {
        RoleResponse response = roleService.updateRole(id, name, description, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    /**
     * Add permission to role
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId
    ) {
        roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission added to role successfully", null));
    }

    /**
     * Remove permission from role
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId
    ) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully", null));
    }

    /**
     * Bulk update role permissions
     */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsRequest request
    ) {
        RoleResponse response = roleService.updateRolePermissions(roleId, request.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", response));
    }
}
