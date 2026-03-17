package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "snapshot_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @Column(name = "ai_event_id")
    private Long aiEventId;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(length = 500)
    private String fileName;

    private Long fileSize;

    @Column(length = 20)
    private String format; // JPEG, PNG, BMP

    @Column(length = 50)
    private String resolution;

    @Column(length = 50)
    private String captureType; // MANUAL, SCHEDULED, EVENT_TRIGGERED

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime capturedAt;
}
