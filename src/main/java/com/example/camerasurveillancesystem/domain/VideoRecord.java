package com.example.camerasurveillancesystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private CameraStream stream;

    @Column(name = "ai_event_id")
    private Long aiEventId;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(length = 500)
    private String fileName;

    private Long fileSize;

    @Column(length = 20)
    private String format; // MP4, AVI, MKV

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private Integer duration; // in seconds

    @Column(length = 50)
    private String resolution;

    private Integer frameRate;

    @Column(nullable = false)
    private Boolean isArchived = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
