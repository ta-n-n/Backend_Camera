package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.CameraGroupCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraGroupUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraGroupResponse;

import java.util.List;

public interface CameraGroupService {

    CameraGroupResponse createGroup(CameraGroupCreateRequest request);

    CameraGroupResponse updateGroup(Long id, CameraGroupUpdateRequest request);

    CameraGroupResponse getGroupById(Long id);

    List<CameraGroupResponse> getAllGroups();

    void deleteGroup(Long id);

    void deleteMultipleGroups(List<Long> ids);

    CameraGroupResponse addCamerasToGroup(Long groupId, List<Long> cameraIds);

    CameraGroupResponse removeCamerasFromGroup(Long groupId, List<Long> cameraIds);
}
