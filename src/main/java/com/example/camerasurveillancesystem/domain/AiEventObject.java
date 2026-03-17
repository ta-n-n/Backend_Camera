package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_event_objects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiEventObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_event_id", nullable = false)
    private AiEvent aiEvent;

    @Column(nullable = false, length = 50)
    private String objectType; // PERSON, CAR, TRUCK, BIKE, ANIMAL, FACE

    @Column(nullable = false)
    private Double confidence;

    @Column(length = 100)
    private String label;

    // Bounding box coordinates
    private Integer boundingBoxX;
    
    private Integer boundingBoxY;
    
    private Integer boundingBoxWidth;
    
    private Integer boundingBoxHeight;

    @Column(columnDefinition = "TEXT")
    private String attributes; // JSON format for additional attributes

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
