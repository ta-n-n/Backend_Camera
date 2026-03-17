package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.CameraDeleteMultipleRequest;
import com.example.camerasurveillancesystem.dto.request.CameraGroupCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraGroupUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.CameraGroupResponse;
import com.example.camerasurveillancesystem.service.CameraGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camera-groups")
@RequiredArgsConstructor
@Slf4j
public class CameraGroupController {

    private final CameraGroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<CameraGroupResponse>> createGroup(
            @Valid @RequestBody CameraGroupCreateRequest request) {
        log.info("Request to create camera group: {}", request.getName());
        
        CameraGroupResponse response = groupService.createGroup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo nhóm thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraGroupResponse>> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody CameraGroupUpdateRequest request) {
        log.info("Request to update camera group ID: {}", id);
        
        CameraGroupResponse response = groupService.updateGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhóm thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CameraGroupResponse>> getGroupById(@PathVariable Long id) {
        log.info("Request to get camera group by ID: {}", id);
        
        CameraGroupResponse response = groupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CameraGroupResponse>>> getAllGroups() {
        log.info("Request to get all camera groups");
        
        List<CameraGroupResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        log.info("Request to delete camera group ID: {}", id);
        
        groupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhóm thành công", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleGroups(
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to delete multiple camera groups: {} groups", request.getIds().size());
        
        groupService.deleteMultipleGroups(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thành công " + request.getIds().size() + " nhóm", null));
    }

    @PostMapping("/{groupId}/cameras")
    public ResponseEntity<ApiResponse<CameraGroupResponse>> addCamerasToGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to add {} cameras to group ID: {}", request.getIds().size(), groupId);
        
        CameraGroupResponse response = groupService.addCamerasToGroup(groupId, request.getIds());
        return ResponseEntity.ok(ApiResponse.success("Thêm camera vào nhóm thành công", response));
    }

    @DeleteMapping("/{groupId}/cameras")
    public ResponseEntity<ApiResponse<CameraGroupResponse>> removeCamerasFromGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody CameraDeleteMultipleRequest request) {
        log.info("Request to remove {} cameras from group ID: {}", request.getIds().size(), groupId);
        
        CameraGroupResponse response = groupService.removeCamerasFromGroup(groupId, request.getIds());
        return ResponseEntity.ok(ApiResponse.success("Xóa camera khỏi nhóm thành công", response));
    }
}
