package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity ghi log hoạt động của người dùng
 * Audit trail cho các thao tác quan trọng trong hệ thống
 */
@Entity
@Table(name = "user_activity_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Username (lưu thêm để tránh null khi user bị xóa)
    @Column(length = 100)
    private String username;

    // Loại hành động: LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW, EXPORT, etc.
    @Column(nullable = false, length = 50)
    private String action;

    // Resource bị tác động: CAMERA, ALERT, USER, ROLE, PERMISSION, VIDEO, etc.
    @Column(length = 50)
    private String resourceType;

    // ID của resource
    private Long resourceId;

    // Mô tả chi tiết hành động
    @Column(columnDefinition = "TEXT")
    private String description;

    // IP address của người dùng
    @Column(length = 45)
    private String ipAddress;

    // User agent (browser, device info)
    @Column(length = 500)
    private String userAgent;

    // HTTP method (GET, POST, PUT, DELETE)
    @Column(length = 10)
    private String httpMethod;

    // Request URL
    @Column(length = 500)
    private String requestUrl;

    // Response status code
    private Integer responseStatus;

    // Thời gian xử lý request (milliseconds)
    private Long executionTime;

    // Dữ liệu cũ (trước khi update/delete) - JSON format
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    // Dữ liệu mới (sau khi create/update) - JSON format
    @Column(columnDefinition = "TEXT")
    private String newValue;

    // Trạng thái: SUCCESS, FAILED
    @Column(length = 20)
    private String status;

    // Error message (nếu có)
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
