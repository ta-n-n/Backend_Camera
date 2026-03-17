package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "camera_health_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraHealthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @Column(nullable = false, length = 20)
    private String status; // ONLINE, OFFLINE, ERROR, WARNING

    @Column(columnDefinition = "TEXT")
    private String message;

    private Integer cpuUsage;

    private Integer memoryUsage;

    private Integer diskUsage;

    private Integer temperature;

    private Integer bandwidth;

    @Column(length = 50)
    private String errorCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkedAt;
}
