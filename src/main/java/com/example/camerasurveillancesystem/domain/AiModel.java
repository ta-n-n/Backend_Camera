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
@Table(name = "ai_models")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(nullable = false, length = 50)
    private String type; // OBJECT_DETECTION, FACE_RECOGNITION, MOTION_DETECTION, LICENSE_PLATE

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String modelPath;

    @Column(length = 500)
    private String configPath;

    private Double accuracy;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    private List<AiEvent> aiEvents = new ArrayList<>();
}
