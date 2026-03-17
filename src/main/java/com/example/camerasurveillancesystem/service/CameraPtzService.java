package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.response.PtzCommandResponse;

public interface CameraPtzService {

    PtzCommandResponse panLeft(Long cameraId, Integer speedPercent, Long durationMs);

    PtzCommandResponse panRight(Long cameraId, Integer speedPercent, Long durationMs);

    PtzCommandResponse tiltUp(Long cameraId, Integer speedPercent, Long durationMs);

    PtzCommandResponse tiltDown(Long cameraId, Integer speedPercent, Long durationMs);

    PtzCommandResponse stop(Long cameraId);
}
