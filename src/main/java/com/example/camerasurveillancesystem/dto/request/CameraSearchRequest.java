package com.example.camerasurveillancesystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraSearchRequest {

    private String name;
    private String code;
    private String status;
    private String model;
    private String manufacturer;
    private Long locationId;
    private Long groupId;
    private String resolution;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";
}
