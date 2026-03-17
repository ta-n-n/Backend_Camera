package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraGroup;
import com.example.camerasurveillancesystem.domain.CameraLocation;
import com.example.camerasurveillancesystem.dto.mapper.CameraMapper;
import com.example.camerasurveillancesystem.dto.request.CameraCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraSearchRequest;
import com.example.camerasurveillancesystem.dto.request.CameraUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;
import com.example.camerasurveillancesystem.repository.CameraGroupRepository;
import com.example.camerasurveillancesystem.repository.CameraLocationRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.CameraService;
import com.example.camerasurveillancesystem.specification.CameraSpecification;
import com.example.camerasurveillancesystem.exception.*;
import com.example.camerasurveillancesystem.service.MediaMTXService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CameraServiceImpl implements CameraService {

    private final CameraRepository cameraRepository;
    private final CameraLocationRepository locationRepository;
    private final CameraGroupRepository groupRepository;
    private final CameraMapper cameraMapper;
    private final MediaMTXService mediaMTXService;

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public CameraResponse createCamera(CameraCreateRequest request) {
        log.info("Creating new camera with code: {}", request.getCode());

        // Kiểm tra code đã tồn tại chưa
        if (cameraRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                ErrorCode.CAMERA_CODE_ALREADY_EXISTS,
                "Camera với mã " + request.getCode() + " đã tồn tại"
            );
        }

        Camera camera = new Camera();
        camera.setName(trimToNull(request.getName()));
        camera.setCode(trimToNull(request.getCode()));
        camera.setModel(trimToNull(request.getModel()));
        camera.setManufacturer(trimToNull(request.getManufacturer()));
        camera.setRtspUrl(trimToNull(request.getRtspUrl()));
        camera.setSnapshotUrl(trimToNull(request.getSnapshotUrl()));
        camera.setStatus(trimToNull(request.getStatus()) != null ? trimToNull(request.getStatus()) : "ACTIVE");
        camera.setResolution(trimToNull(request.getResolution()));
        camera.setFrameRate(request.getFrameRate());
        camera.setDescription(trimToNull(request.getDescription()));

        // Set location nếu có
        if (request.getLocationId() != null) {
            CameraLocation location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy location với ID: " + request.getLocationId()
                    ));
            camera.setLocation(location);
        }

        // Set groups nếu có
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            Set<CameraGroup> groups = new HashSet<>(groupRepository.findAllById(request.getGroupIds()));
            camera.setGroups(groups);
        }

        Camera savedCamera = cameraRepository.save(camera);
        log.info("Camera created successfully with ID: {}", savedCamera.getId());

        // Đăng ký camera vào MediaMTX để FE có thể stream qua HLS/WebRTC
        mediaMTXService.registerCamera(savedCamera.getCode(), savedCamera.getRtspUrl());

        CameraResponse response = cameraMapper.toResponse(savedCamera);
        response.setHlsUrl(mediaMTXService.getHlsUrl(savedCamera.getCode()));
        response.setWebrtcUrl(mediaMTXService.getWebRtcUrl(savedCamera.getCode()));
        return response;
    }

    @Override
    public CameraResponse updateCamera(Long id, CameraUpdateRequest request) {
        log.info("Updating camera with ID: {}", id);

        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy camera với ID: " + id
                ));

        // Update fields nếu có
        if (request.getName() != null) {
            camera.setName(trimToNull(request.getName()));
        }
        if (request.getModel() != null) {
            camera.setModel(trimToNull(request.getModel()));
        }
        if (request.getManufacturer() != null) {
            camera.setManufacturer(trimToNull(request.getManufacturer()));
        }
        if (request.getRtspUrl() != null) {
            camera.setRtspUrl(trimToNull(request.getRtspUrl()));
        }
        if (request.getSnapshotUrl() != null) {
            camera.setSnapshotUrl(trimToNull(request.getSnapshotUrl()));
        }
        if (request.getStatus() != null) {
            camera.setStatus(trimToNull(request.getStatus()));
        }
        if (request.getResolution() != null) {
            camera.setResolution(trimToNull(request.getResolution()));
        }
        if (request.getFrameRate() != null) {
            camera.setFrameRate(request.getFrameRate());
        }
        if (request.getDescription() != null) {
            camera.setDescription(trimToNull(request.getDescription()));
        }

        // Update location
        if (request.getLocationId() != null) {
            CameraLocation location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy location với ID: " + request.getLocationId()
                    ));
            camera.setLocation(location);
        }

        // Update groups
        if (request.getGroupIds() != null) {
            Set<CameraGroup> groups = new HashSet<>(groupRepository.findAllById(request.getGroupIds()));
            camera.setGroups(groups);
        }

        Camera updatedCamera = cameraRepository.save(camera);
        log.info("Camera updated successfully with ID: {}", updatedCamera.getId());

        // Cập nhật RTSP source trên MediaMTX nếu URL thay đổi
        if (request.getRtspUrl() != null) {
            mediaMTXService.updateCamera(updatedCamera.getCode(), updatedCamera.getRtspUrl());
        }

        CameraResponse response = cameraMapper.toResponse(updatedCamera);
        response.setHlsUrl(mediaMTXService.getHlsUrl(updatedCamera.getCode()));
        response.setWebrtcUrl(mediaMTXService.getWebRtcUrl(updatedCamera.getCode()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CameraResponse getCameraById(Long id) {
        log.info("Getting camera by ID: {}", id);

        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy camera với ID: " + id
                ));

        CameraResponse response = cameraMapper.toResponse(camera);
        response.setHlsUrl(mediaMTXService.getHlsUrl(camera.getCode()));
        response.setWebrtcUrl(mediaMTXService.getWebRtcUrl(camera.getCode()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CameraResponse getCameraByCode(String code) {
        log.info("Getting camera by code: {}", code);

        Camera camera = cameraRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy camera với mã: " + code
                ));

        CameraResponse response = cameraMapper.toResponse(camera);
        response.setHlsUrl(mediaMTXService.getHlsUrl(camera.getCode()));
        response.setWebrtcUrl(mediaMTXService.getWebRtcUrl(camera.getCode()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraResponse> getAllCameras() {
        log.info("Getting all cameras");

        List<Camera> cameras = cameraRepository.findAll();

        return cameras.stream()
                .map(cam -> {
                    CameraResponse r = cameraMapper.toResponse(cam);
                    r.setHlsUrl(mediaMTXService.getHlsUrl(cam.getCode()));
                    r.setWebrtcUrl(mediaMTXService.getWebRtcUrl(cam.getCode()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CameraResponse> searchCameras(CameraSearchRequest request) {
        log.info("Searching cameras with filters: {}", request);

        // Tạo specification từ request
        Specification<Camera> spec = CameraSpecification.withFilters(request);

        // Tạo pageable
        Sort sort = request.getSortDirection().equalsIgnoreCase("DESC")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Tìm kiếm
        Page<Camera> cameraPage = cameraRepository.findAll(spec, pageable);

        // Convert sang response
        List<CameraResponse> content = cameraPage.getContent().stream()
                .map(cam -> {
                    CameraResponse r = cameraMapper.toResponse(cam);
                    r.setHlsUrl(mediaMTXService.getHlsUrl(cam.getCode()));
                    r.setWebrtcUrl(mediaMTXService.getWebRtcUrl(cam.getCode()));
                    return r;
                })
                .collect(Collectors.toList());

        return PageResponse.<CameraResponse>builder()
                .content(content)
                .pageNumber(cameraPage.getNumber())
                .pageSize(cameraPage.getSize())
                .totalElements(cameraPage.getTotalElements())
                .totalPages(cameraPage.getTotalPages())
                .last(cameraPage.isLast())
                .first(cameraPage.isFirst())
                .build();
    }

    @Override
    public void deleteCamera(Long id) {
        log.info("Deleting camera with ID: {}", id);

        if (!cameraRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Không tìm thấy camera với ID: " + id
            );
        }

        // Xóa stream trên MediaMTX trước khi xóa khỏi DB
        cameraRepository.findById(id).ifPresent(cam ->
            mediaMTXService.unregisterCamera(cam.getCode())
        );

        cameraRepository.deleteById(id);
        log.info("Camera deleted successfully with ID: {}", id);
    }

    @Override
    public void deleteMultipleCameras(List<Long> ids) {
        log.info("Deleting multiple cameras with IDs: {}", ids);

        List<Camera> cameras = cameraRepository.findByIdIn(ids);

        if (cameras.size() != ids.size()) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Một số camera không tồn tại trong hệ thống"
            );
        }

        cameraRepository.deleteAllById(ids);
        log.info("Deleted {} cameras successfully", ids.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return cameraRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return cameraRepository.countByStatus(status);
    }

    @Override
    public CameraResponse updateCameraStatus(Long id, String status) {
        log.info("Updating camera status with ID: {} to status: {}", id, status);

        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Không tìm thấy camera với ID: " + id
                ));

        camera.setStatus(status);
        Camera updatedCamera = cameraRepository.save(camera);

        log.info("Camera status updated successfully with ID: {}", updatedCamera.getId());

        CameraResponse response = cameraMapper.toResponse(updatedCamera);
        response.setHlsUrl(mediaMTXService.getHlsUrl(updatedCamera.getCode()));
        response.setWebrtcUrl(mediaMTXService.getWebRtcUrl(updatedCamera.getCode()));
        return response;
    }
}
