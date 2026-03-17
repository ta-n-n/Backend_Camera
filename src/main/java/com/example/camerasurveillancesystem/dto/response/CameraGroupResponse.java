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
public class CameraGroupResponse {

    private Long id;
    private String name;
    private String description;
    private Integer cameraCount;
    private Set<CameraBasicInfo> cameras;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CameraBasicInfo {
        private Long id;
        private String name;
        private String code;
        private String status;
    }
}
