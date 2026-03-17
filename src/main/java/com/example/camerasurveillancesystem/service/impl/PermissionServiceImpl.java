package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.domain.Permission;
import com.example.camerasurveillancesystem.dto.response.PermissionResponse;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.PermissionRepository;
import com.example.camerasurveillancesystem.service.PermissionService;
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
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PERMISSION_NOT_FOUND));
        return convertToResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(String name, String description) {
        if (permissionRepository.existsByName(name)) {
            throw new RuntimeException("Permission already exists");
        }

        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);

        permission = permissionRepository.save(permission);
        return convertToResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, String name, String description) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PERMISSION_NOT_FOUND));

        if (name != null) {
            permission.setName(name);
        }
        if (description != null) {
            permission.setDescription(description);
        }

        permission = permissionRepository.save(permission);
        return convertToResponse(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        permissionRepository.deleteById(id);
    }

    private PermissionResponse convertToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
