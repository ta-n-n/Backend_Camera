package com.example.camerasurveillancesystem.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraUpdateRequest {

    @Size(max = 100, message = "Tên camera không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 100, message = "Model không được vượt quá 100 ký tự")
    private String model;

    @Size(max = 100, message = "Nhà sản xuất không được vượt quá 100 ký tự")
    private String manufacturer;

    @Size(max = 500, message = "RTSP URL không được vượt quá 500 ký tự")
    private String rtspUrl;

    @Size(max = 500, message = "Snapshot URL không được vượt quá 500 ký tự")
    private String snapshotUrl;

    @Pattern(regexp = "ACTIVE|INACTIVE|MAINTENANCE|ERROR", message = "Trạng thái phải là ACTIVE, INACTIVE, MAINTENANCE hoặc ERROR")
    private String status;

    @Size(max = 50, message = "Độ phân giải không được vượt quá 50 ký tự")
    private String resolution;

    private Integer frameRate;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private Long locationId;

    private Set<Long> groupIds;
}
