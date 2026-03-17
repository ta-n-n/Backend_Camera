package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PermissionResponse;
import com.example.camerasurveillancesystem.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Get all permissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> response = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get permission by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Create new permission
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @RequestParam String name,
            @RequestParam(required = false) String description
    ) {
        PermissionResponse response = permissionService.createPermission(name, description);
        return ResponseEntity.ok(ApiResponse.success("Permission created successfully", response));
    }

    /**
     * Update permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ) {
        PermissionResponse response = permissionService.updatePermission(id, name, description);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", response));
    }

    /**
     * Delete permission
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully", null));
    }
}
