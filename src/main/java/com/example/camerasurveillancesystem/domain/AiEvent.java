package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private AiModel model;

    @Column(nullable = false, length = 50)
    private String eventType; // PERSON_DETECTED, VEHICLE_DETECTED, INTRUSION, LOITERING, CROWD

    @Column(nullable = false)
    private Double confidenceScore;

    @Column(length = 500)
    private String snapshotPath;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON format for additional data

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "aiEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiEventObject> detectedObjects = new ArrayList<>();

    @OneToMany(mappedBy = "aiEvent", cascade = CascadeType.ALL)
    private List<Alert> alerts = new ArrayList<>();
}
