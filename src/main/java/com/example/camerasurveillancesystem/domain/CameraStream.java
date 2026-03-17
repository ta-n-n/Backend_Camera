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
@Table(name = "camera_streams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @Column(nullable = false, length = 20)
    private String streamType; // LIVE, PLAYBACK, SNAPSHOT

    @Column(nullable = false, length = 500)
    private String streamUrl;

    @Column(length = 20)
    private String protocol; // RTSP, RTMP, HLS, WebRTC

    @Column(nullable = false, length = 20)
    private String quality; // HIGH, MEDIUM, LOW

    @Column(length = 50)
    private String resolution;

    private Integer bitrate;

    private Integer frameRate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "stream", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoRecord> videoRecords = new ArrayList<>();
}
