package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.domain.Permission;
import com.example.camerasurveillancesystem.domain.Role;
import com.example.camerasurveillancesystem.dto.response.PermissionResponse;
import com.example.camerasurveillancesystem.dto.response.RoleResponse;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.PermissionRepository;
import com.example.camerasurveillancesystem.repository.RoleRepository;
import com.example.camerasurveillancesystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final com.example.camerasurveillancesystem.repository.UserRepository userRepository;

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));
        return convertToResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse createRole(String name, String description, Set<Long> permissionIds) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists");
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        return convertToResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, String name, String description, Set<Long> permissionIds) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        if (name != null) {
            role.setName(name);
        }
        if (description != null) {
            role.setDescription(description);
        }

        if (permissionIds != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        return convertToResponse(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PERMISSION_NOT_FOUND));

        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        if (permissionIds != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            role.setPermissions(permissions);
        } else {
            role.getPermissions().clear();
        }

        role = roleRepository.save(role);
        return convertToResponse(role);
    }

    private RoleResponse convertToResponse(Role role) {
        long userCount = userRepository.countByRolesId(role.getId());
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(this::convertToPermissionResponse)
                        .collect(Collectors.toSet()))
                .userCount(userCount)
                .build();
    }

    private PermissionResponse convertToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
