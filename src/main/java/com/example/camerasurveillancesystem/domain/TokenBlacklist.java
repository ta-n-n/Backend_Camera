package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ các JWT token đã bị vô hiệu hóa (blacklisted)
 * Sử dụng cho chức năng logout
 */
@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_expiry_date", columnList = "expiry_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(length = 100)
    private String username;

    // Thời điểm token hết hạn (để cleanup)
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Lý do blacklist: LOGOUT, FORCE_LOGOUT, SECURITY_BREACH
    @Column(length = 50)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
