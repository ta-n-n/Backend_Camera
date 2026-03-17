package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraStream;
import com.example.camerasurveillancesystem.dto.mapper.CameraStreamMapper;
import com.example.camerasurveillancesystem.dto.request.CameraStreamCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraStreamUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraStreamResponse;
import com.example.camerasurveillancesystem.exception.*;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.repository.CameraStreamRepository;
import com.example.camerasurveillancesystem.service.CameraStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CameraStreamServiceImpl implements CameraStreamService {

    private final CameraStreamRepository streamRepository;
    private final CameraRepository cameraRepository;
    private final CameraStreamMapper streamMapper;

    @Override
    public CameraStreamResponse createStream(CameraStreamCreateRequest request) {
        log.info("Creating new camera stream for camera ID: {}", request.getCameraId());

        Camera camera = cameraRepository.findById(request.getCameraId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy camera với ID: " + request.getCameraId()
                ));

        CameraStream stream = new CameraStream();
        stream.setCamera(camera);
        stream.setStreamType(request.getStreamType());
        stream.setStreamUrl(request.getStreamUrl());
        stream.setProtocol(request.getProtocol());
        stream.setQuality(request.getQuality());
        stream.setResolution(request.getResolution());
        stream.setBitrate(request.getBitrate());
        stream.setFrameRate(request.getFrameRate());
        stream.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        CameraStream savedStream = streamRepository.save(stream);
        log.info("Camera stream created successfully with ID: {}", savedStream.getId());

        return streamMapper.toResponse(savedStream);
    }

    @Override
    public CameraStreamResponse updateStream(Long id, CameraStreamUpdateRequest request) {
        log.info("Updating camera stream with ID: {}", id);

        CameraStream stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.STREAM_NOT_FOUND,
                    "Không tìm thấy stream với ID: " + id
                ));

        if (request.getStreamType() != null) {
            stream.setStreamType(request.getStreamType());
        }
        if (request.getStreamUrl() != null) {
            stream.setStreamUrl(request.getStreamUrl());
        }
        if (request.getProtocol() != null) {
            stream.setProtocol(request.getProtocol());
        }
        if (request.getQuality() != null) {
            stream.setQuality(request.getQuality());
        }
        if (request.getResolution() != null) {
            stream.setResolution(request.getResolution());
        }
        if (request.getBitrate() != null) {
            stream.setBitrate(request.getBitrate());
        }
        if (request.getFrameRate() != null) {
            stream.setFrameRate(request.getFrameRate());
        }
        if (request.getIsActive() != null) {
            stream.setIsActive(request.getIsActive());
        }

        CameraStream updatedStream = streamRepository.save(stream);
        log.info("Camera stream updated successfully with ID: {}", updatedStream.getId());

        return streamMapper.toResponse(updatedStream);
    }

    @Override
    @Transactional(readOnly = true)
    public CameraStreamResponse getStreamById(Long id) {
        log.info("Getting camera stream by ID: {}", id);

        CameraStream stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.STREAM_NOT_FOUND,
                    "Không tìm thấy stream với ID: " + id
                ));

        return streamMapper.toResponse(stream);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraStreamResponse> getAllStreams() {
        log.info("Getting all camera streams");

        List<CameraStream> streams = streamRepository.findAll();

        return streams.stream()
                .map(streamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraStreamResponse> getStreamsByCameraId(Long cameraId) {
        log.info("Getting streams for camera ID: {}", cameraId);

        // Verify camera exists
        if (!cameraRepository.existsById(cameraId)) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy camera với ID: " + cameraId
            );
        }

        List<CameraStream> streams = streamRepository.findByCameraId(cameraId);

        return streams.stream()
                .map(streamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraStreamResponse> getStreamsByType(String streamType) {
        log.info("Getting streams by type: {}", streamType);

        List<CameraStream> streams = streamRepository.findByStreamType(streamType);

        return streams.stream()
                .map(streamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraStreamResponse> getActiveStreams() {
        log.info("Getting all active streams");

        List<CameraStream> streams = streamRepository.findByIsActive(true);

        return streams.stream()
                .map(streamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStream(Long id) {
        log.info("Deleting camera stream with ID: {}", id);

        if (!streamRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                ErrorCode.STREAM_NOT_FOUND,
                "Không tìm thấy stream với ID: " + id
            );
        }

        streamRepository.deleteById(id);
        log.info("Camera stream deleted successfully with ID: {}", id);
    }

    @Override
    public void deleteMultipleStreams(List<Long> ids) {
        log.info("Deleting multiple camera streams with IDs: {}", ids);

        List<CameraStream> streams = streamRepository.findByIdIn(ids);

        if (streams.size() != ids.size()) {
            throw new ResourceNotFoundException(
                ErrorCode.STREAM_NOT_FOUND,
                "Một số stream không tồn tại trong hệ thống"
            );
        }

        streamRepository.deleteAllById(ids);
        log.info("Deleted {} camera streams successfully", ids.size());
    }

    @Override
    public CameraStreamResponse toggleStreamStatus(Long id) {
        log.info("Toggling stream status for ID: {}", id);

        CameraStream stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.STREAM_NOT_FOUND,
                    "Không tìm thấy stream với ID: " + id
                ));

        stream.setIsActive(!stream.getIsActive());
        CameraStream updatedStream = streamRepository.save(stream);

        log.info("Stream status toggled to: {}", updatedStream.getIsActive());
        return streamMapper.toResponse(updatedStream);
    }
}
