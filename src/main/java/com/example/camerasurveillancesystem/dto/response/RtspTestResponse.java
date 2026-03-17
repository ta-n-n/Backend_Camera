package com.example.camerasurveillancesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RtspTestResponse {

    private boolean success;
    private String message;
    private String diagnosticCode;
    private String rtspUrl;
    private String normalizedRtspUrl;
    /** Thời gian kết nối (ms) */
    private Long responseTimeMs;
}
