package com.example.camerasurveillancesystem.dto.response.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageStatistics {

    private Long totalVideos;
    private Long totalSnapshots;
    private Long totalVideoSizeBytes;
    private Long totalSnapshotSizeBytes;
    private String totalVideoSizeGB;
    private String totalSnapshotSizeGB;
    private String totalStorageSizeGB;
    private Double averageVideoSizeMB;
    private Integer averageVideoDurationSeconds;
}
