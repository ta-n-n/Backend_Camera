package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraGroup;
import com.example.camerasurveillancesystem.dto.mapper.CameraGroupMapper;
import com.example.camerasurveillancesystem.dto.request.CameraGroupCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraGroupUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraGroupResponse;
import com.example.camerasurveillancesystem.exception.*;
import com.example.camerasurveillancesystem.repository.CameraGroupRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.CameraGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CameraGroupServiceImpl implements CameraGroupService {

    private final CameraGroupRepository groupRepository;
    private final CameraRepository cameraRepository;
    private final CameraGroupMapper groupMapper;

    @Override
    public CameraGroupResponse createGroup(CameraGroupCreateRequest request) {
        log.info("Creating new camera group: {}", request.getName());

        CameraGroup group = new CameraGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());

        // Add cameras if provided
        if (request.getCameraIds() != null && !request.getCameraIds().isEmpty()) {
            Set<Camera> cameras = new HashSet<>(cameraRepository.findAllById(request.getCameraIds()));
            if (cameras.size() != request.getCameraIds().size()) {
                throw new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Một số camera không tồn tại trong hệ thống"
                );
            }
            group.setCameras(cameras);
        }

        CameraGroup savedGroup = groupRepository.save(group);
        log.info("Camera group created successfully with ID: {}", savedGroup.getId());

        return groupMapper.toResponse(savedGroup);
    }

    @Override
    public CameraGroupResponse updateGroup(Long id, CameraGroupUpdateRequest request) {
        log.info("Updating camera group with ID: {}", id);

        CameraGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.GROUP_NOT_FOUND,
                    "Không tìm thấy nhóm với ID: " + id
                ));

        if (request.getName() != null) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        // Update cameras if provided
        if (request.getCameraIds() != null) {
            Set<Camera> cameras = new HashSet<>(cameraRepository.findAllById(request.getCameraIds()));
            if (cameras.size() != request.getCameraIds().size()) {
                throw new ResourceNotFoundException(
                    ErrorCode.CAMERA_NOT_FOUND,
                    "Một số camera không tồn tại trong hệ thống"
                );
            }
            group.setCameras(cameras);
        }

        CameraGroup updatedGroup = groupRepository.save(group);
        log.info("Camera group updated successfully with ID: {}", updatedGroup.getId());

        return groupMapper.toResponse(updatedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public CameraGroupResponse getGroupById(Long id) {
        log.info("Getting camera group by ID: {}", id);

        CameraGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.GROUP_NOT_FOUND,
                    "Không tìm thấy nhóm với ID: " + id
                ));

        return groupMapper.toResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraGroupResponse> getAllGroups() {
        log.info("Getting all camera groups");

        List<CameraGroup> groups = groupRepository.findAll();

        return groups.stream()
                .map(groupMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGroup(Long id) {
        log.info("Deleting camera group with ID: {}", id);

        if (!groupRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                ErrorCode.GROUP_NOT_FOUND,
                "Không tìm thấy nhóm với ID: " + id
            );
        }

        groupRepository.deleteById(id);
        log.info("Camera group deleted successfully with ID: {}", id);
    }

    @Override
    public void deleteMultipleGroups(List<Long> ids) {
        log.info("Deleting multiple camera groups with IDs: {}", ids);

        List<CameraGroup> groups = groupRepository.findAllById(ids);

        if (groups.size() != ids.size()) {
            throw new ResourceNotFoundException(
                ErrorCode.GROUP_NOT_FOUND,
                "Một số nhóm không tồn tại trong hệ thống"
            );
        }

        groupRepository.deleteAllById(ids);
        log.info("Deleted {} camera groups successfully", ids.size());
    }

    @Override
    public CameraGroupResponse addCamerasToGroup(Long groupId, List<Long> cameraIds) {
        log.info("Adding {} cameras to group ID: {}", cameraIds.size(), groupId);

        CameraGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.GROUP_NOT_FOUND,
                    "Không tìm thấy nhóm với ID: " + groupId
                ));

        List<Camera> cameras = cameraRepository.findByIdIn(cameraIds);
        if (cameras.size() != cameraIds.size()) {
            throw new ResourceNotFoundException(
                ErrorCode.CAMERA_NOT_FOUND,
                "Một số camera không tồn tại trong hệ thống"
            );
        }

        group.getCameras().addAll(cameras);
        CameraGroup updatedGroup = groupRepository.save(group);

        log.info("Added {} cameras to group successfully", cameraIds.size());
        return groupMapper.toResponse(updatedGroup);
    }

    @Override
    public CameraGroupResponse removeCamerasFromGroup(Long groupId, List<Long> cameraIds) {
        log.info("Removing {} cameras from group ID: {}", cameraIds.size(), groupId);

        CameraGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.GROUP_NOT_FOUND,
                    "Không tìm thấy nhóm với ID: " + groupId
                ));

        List<Camera> camerasToRemove = cameraRepository.findByIdIn(cameraIds);
        group.getCameras().removeAll(camerasToRemove);

        CameraGroup updatedGroup = groupRepository.save(group);

        log.info("Removed {} cameras from group successfully", cameraIds.size());
        return groupMapper.toResponse(updatedGroup);
    }
}
