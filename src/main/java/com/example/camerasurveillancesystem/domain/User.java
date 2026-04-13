package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity Người dùng
 * Quản lý thông tin tài khoản người dùng trong hệ thống giám sát camera
 * Hỗ trợ đăng nhập thông thường (username/password) và đăng nhập qua OAuth2 (Google, Facebook...)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên đăng nhập (duy nhất)
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // Email (duy nhất, bắt buộc)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column
    private String displayName;
    // Mật khẩu (nullable cho trường hợp đăng nhập bằng Google/Facebook)
    @Column
    private String password;

    // Họ và tên đầy đủ
    @Column(length = 100)
    private String fullName;

    // Số điện thoại liên hệ
    @Column(length = 20)
    private String phoneNumber;

    // URL ảnh đại diện (từ OAuth hoặc upload)
    @Column(length = 500)
    private String avatar;

    // Phương thức đăng nhập: LOCAL, GOOGLE, FACEBOOK, GITHUB
    @Column(length = 50)
    private String authProvider;

    // ID người dùng từ nhà cung cấp OAuth (Google ID, Facebook ID...)
    @Column(length = 255)
    private String providerId;

    // Dữ liệu bổ sung từ provider (lưu dạng JSON)
    @Column(columnDefinition = "TEXT")
    private String providerData;

    // Trạng thái tài khoản (true: đang hoạt động, false: bị khóa)
    @Column(nullable = false)
    private Boolean isActive = true;

    // Email đã được xác thực chưa
    @Column(nullable = false)
    private Boolean isEmailVerified = false;

    // Thời gian đăng nhập lần cuối
    private LocalDateTime lastLoginAt;

    // Thời gian tạo tài khoản
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Thời gian cập nhật thông tin
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Danh sách vai trò của người dùng (Many-to-Many với Role)
    // VD: 1 user có thể là cả ADMIN và OPERATOR
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
