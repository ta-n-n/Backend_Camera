package com.example.camerasurveillancesystem.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiEventObjectResponse {

    private Long id;
    private Long aiEventId;
    private String objectType;
    private Double confidence;
    private String label;
    private Integer boundingBoxX;
    private Integer boundingBoxY;
    private Integer boundingBoxWidth;
    private Integer boundingBoxHeight;
    private String attributes;
    private LocalDateTime createdAt;
}
