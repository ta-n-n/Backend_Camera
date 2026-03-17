package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.CameraStreamCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraStreamUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraStreamResponse;

import java.util.List;

public interface CameraStreamService {

    CameraStreamResponse createStream(CameraStreamCreateRequest request);

    CameraStreamResponse updateStream(Long id, CameraStreamUpdateRequest request);

    CameraStreamResponse getStreamById(Long id);

    List<CameraStreamResponse> getAllStreams();

    List<CameraStreamResponse> getStreamsByCameraId(Long cameraId);

    List<CameraStreamResponse> getStreamsByType(String streamType);

    List<CameraStreamResponse> getActiveStreams();

    void deleteStream(Long id);

    void deleteMultipleStreams(List<Long> ids);

    CameraStreamResponse toggleStreamStatus(Long id);
}
