package com.example.camerasurveillancesystem.config;

import com.example.camerasurveillancesystem.domain.Permission;
import com.example.camerasurveillancesystem.domain.Role;
import com.example.camerasurveillancesystem.domain.User;
import com.example.camerasurveillancesystem.repository.PermissionRepository;
import com.example.camerasurveillancesystem.repository.RoleRepository;
import com.example.camerasurveillancesystem.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Khởi tạo dữ liệu ban đầu cho hệ thống
 * - Permissions
 * - Roles (ADMIN, MANAGER, OPERATOR, VIEWER)
 * - Admin user mặc định
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initData() {
        log.info("=== Starting Data Initialization ===");

        // 1. Initialize Permissions
        initPermissions();

        // 2. Initialize Roles
        initRoles();

        // 3. Initialize Admin User
        initAdminUser();

        log.info("=== Data Initialization Completed ===");
    }

    private void initPermissions() {
        if (permissionRepository.count() > 0) {
            log.info("Permissions already exist. Skipping initialization.");
            return;
        }

        log.info("Initializing permissions...");

        List<Permission> permissions = Arrays.asList(
                // User Management
                createPermission("USER_READ", "Xem thông tin người dùng"),
                createPermission("USER_CREATE", "Tạo người dùng mới"),
                createPermission("USER_UPDATE", "Cập nhật người dùng"),
                createPermission("USER_DELETE", "Xóa người dùng"),

                // Role Management
                createPermission("ROLE_READ", "Xem vai trò"),
                createPermission("ROLE_CREATE", "Tạo vai trò"),
                createPermission("ROLE_UPDATE", "Cập nhật vai trò"),
                createPermission("ROLE_DELETE", "Xóa vai trò"),

                // Permission Management
                createPermission("PERMISSION_READ", "Xem quyền"),
                createPermission("PERMISSION_CREATE", "Tạo quyền"),
                createPermission("PERMISSION_UPDATE", "Cập nhật quyền"),
                createPermission("PERMISSION_DELETE", "Xóa quyền"),

                // Camera Management
                createPermission("CAMERA_READ", "Xem camera"),
                createPermission("CAMERA_CREATE", "Tạo camera"),
                createPermission("CAMERA_UPDATE", "Cập nhật camera"),
                createPermission("CAMERA_DELETE", "Xóa camera"),
                createPermission("CAMERA_CONTROL", "Điều khiển camera"),

                // Video & Snapshot
                createPermission("VIDEO_READ", "Xem video"),
                createPermission("VIDEO_DELETE", "Xóa video"),
                createPermission("SNAPSHOT_READ", "Xem snapshot"),
                createPermission("SNAPSHOT_CREATE", "Tạo snapshot"),
                createPermission("SNAPSHOT_DELETE", "Xóa snapshot"),

                // Alert Management
                createPermission("ALERT_READ", "Xem cảnh báo"),
                createPermission("ALERT_CREATE", "Tạo cảnh báo"),
                createPermission("ALERT_UPDATE", "Cập nhật cảnh báo"),
                createPermission("ALERT_DELETE", "Xóa cảnh báo"),
                createPermission("ALERT_ASSIGN", "Phân công cảnh báo"),

                // AI & Processing
                createPermission("AI_MODEL_READ", "Xem mô hình AI"),
                createPermission("AI_MODEL_UPDATE", "Cập nhật mô hình AI"),
                createPermission("AI_EVENT_READ", "Xem sự kiện AI"),

                // System
                createPermission("SYSTEM_CONFIG", "Cấu hình hệ thống"),
                createPermission("SYSTEM_BACKUP", "Sao lưu hệ thống"),
                createPermission("SYSTEM_RESTORE", "Khôi phục hệ thống"),
                createPermission("ACTIVITY_LOG_READ", "Xem nhật ký hoạt động"),

                // Reports
                createPermission("REPORT_READ", "Xem báo cáo"),
                createPermission("REPORT_EXPORT", "Xuất báo cáo"),

                // Admin privilege
                createPermission("ADMIN", "Quyền quản trị viên toàn quyền")
        );

        permissionRepository.saveAll(permissions);
        log.info("Created {} permissions", permissions.size());
    }

    private void initRoles() {
        if (roleRepository.count() > 0) {
            log.info("Roles already exist. Skipping initialization.");
            return;
        }

        log.info("Initializing roles...");

        // ADMIN - Full access
        Role adminRole = createRole("ROLE_ADMIN", "Quản trị viên", Arrays.asList(
                "ADMIN", // Admin có tất cả quyền
                "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
                "ROLE_READ", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE",
                "PERMISSION_READ", "PERMISSION_CREATE", "PERMISSION_UPDATE", "PERMISSION_DELETE",
                "CAMERA_READ", "CAMERA_CREATE", "CAMERA_UPDATE", "CAMERA_DELETE", "CAMERA_CONTROL",
                "VIDEO_READ", "VIDEO_DELETE",
                "SNAPSHOT_READ", "SNAPSHOT_CREATE", "SNAPSHOT_DELETE",
                "ALERT_READ", "ALERT_CREATE", "ALERT_UPDATE", "ALERT_DELETE", "ALERT_ASSIGN",
                "AI_MODEL_READ", "AI_MODEL_UPDATE", "AI_EVENT_READ",
                "SYSTEM_CONFIG", "SYSTEM_BACKUP", "SYSTEM_RESTORE",
                "ACTIVITY_LOG_READ",
                "REPORT_READ", "REPORT_EXPORT"
        ));

        // MANAGER - Management level access
        Role managerRole = createRole("ROLE_MANAGER", "Quản lý", Arrays.asList(
                "USER_READ", "USER_CREATE", "USER_UPDATE",
                "CAMERA_READ", "CAMERA_CREATE", "CAMERA_UPDATE", "CAMERA_CONTROL",
                "VIDEO_READ", "VIDEO_DELETE",
                "SNAPSHOT_READ", "SNAPSHOT_CREATE", "SNAPSHOT_DELETE",
                "ALERT_READ", "ALERT_CREATE", "ALERT_UPDATE", "ALERT_ASSIGN",
                "AI_MODEL_READ", "AI_EVENT_READ",
                "ACTIVITY_LOG_READ",
                "REPORT_READ", "REPORT_EXPORT"
        ));

        // OPERATOR - Operational access
        Role operatorRole = createRole("ROLE_OPERATOR", "Vận hành viên", Arrays.asList(
                "CAMERA_READ", "CAMERA_UPDATE", "CAMERA_CONTROL",
                "VIDEO_READ",
                "SNAPSHOT_READ", "SNAPSHOT_CREATE",
                "ALERT_READ", "ALERT_UPDATE",
                "AI_EVENT_READ",
                "REPORT_READ"
        ));

        // VIEWER - Read-only access
        Role viewerRole = createRole("ROLE_VIEWER", "Người xem", Arrays.asList(
                "CAMERA_READ",
                "VIDEO_READ",
                "SNAPSHOT_READ",
                "ALERT_READ",
                "AI_EVENT_READ",
                "REPORT_READ"
        ));

        roleRepository.saveAll(Arrays.asList(adminRole, managerRole, operatorRole, viewerRole));
        log.info("Created 4 roles: ADMIN, MANAGER, OPERATOR, VIEWER");
    }

    private void initAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists. Skipping initialization.");
            return;
        }

        log.info("Creating admin user...");

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@camerasurveillance.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Administrator");
        admin.setIsActive(true);
        admin.setIsEmailVerified(true);
        admin.setAuthProvider("LOCAL");

        // Assign ADMIN role
        roleRepository.findByName("ROLE_ADMIN").ifPresent(role -> {
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            admin.setRoles(roles);
        });

        userRepository.save(admin);
        log.info("Admin user created successfully");
        log.info("  Username: admin");
        log.info("  Password: admin123");
        log.info("  Email: admin@camerasurveillance.com");
    }

    private Permission createPermission(String name, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        return permission;
    }

    private Role createRole(String name, String description, List<String> permissionNames) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);

        Set<Permission> permissions = new HashSet<>();
        for (String permName : permissionNames) {
            Permission permission = permissionRepository.findByName(permName).orElse(null);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);

        return role;
    }
}
