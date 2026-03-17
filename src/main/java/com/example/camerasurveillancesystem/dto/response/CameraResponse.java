package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraResponse {

    private Long id;
    private String name;
    private String code;
    private String model;
    private String manufacturer;
    private String rtspUrl;
    private String snapshotUrl;
    private String status;
    private String resolution;
    private Integer frameRate;
    private String description;

    // MediaMTX stream URLs - FE dùng để phát video
    private String hlsUrl;     // http://localhost:8888/{code}/index.m3u8
    private String webrtcUrl;  // http://localhost:8889/{code}

    private CameraLocationResponse location;
    private Set<CameraGroupResponse> groups;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CameraLocationResponse {
        private Long id;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private String city;
        private String district;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CameraGroupResponse {
        private Long id;
        private String name;
        private String description;
    }
}
