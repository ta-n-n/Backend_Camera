package com.example.camerasurveillancesystem.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RtspConnectionTestResult {

    private boolean success;
    private String diagnosticCode;
    private String diagnosticMessage;
    private String normalizedRtspUrl;
}
