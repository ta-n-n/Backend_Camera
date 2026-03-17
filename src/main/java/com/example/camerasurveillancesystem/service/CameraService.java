package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.dto.request.CameraCreateRequest;
import com.example.camerasurveillancesystem.dto.request.CameraSearchRequest;
import com.example.camerasurveillancesystem.dto.request.CameraUpdateRequest;
import com.example.camerasurveillancesystem.dto.response.CameraResponse;
import com.example.camerasurveillancesystem.dto.response.PageResponse;

import java.util.List;

public interface CameraService {

    /**
     * Tạo camera mới
     */
    CameraResponse createCamera(CameraCreateRequest request);

    /**
     * Cập nhật thông tin camera
     */
    CameraResponse updateCamera(Long id, CameraUpdateRequest request);

    /**
     * Lấy thông tin chi tiết camera theo ID
     */
    CameraResponse getCameraById(Long id);

    /**
     * Lấy thông tin camera theo code
     */
    CameraResponse getCameraByCode(String code);

    /**
     * Lấy tất cả camera
     */
    List<CameraResponse> getAllCameras();

    /**
     * Tìm kiếm camera với phân trang và lọc
     */
    PageResponse<CameraResponse> searchCameras(CameraSearchRequest request);

    /**
     * Xóa camera theo ID
     */
    void deleteCamera(Long id);

    /**
     * Xóa nhiều camera
     */
    void deleteMultipleCameras(List<Long> ids);

    /**
     * Kiểm tra camera có tồn tại không
     */
    boolean existsByCode(String code);

    /**
     * Đếm số camera theo trạng thái
     */
    long countByStatus(String status);

    /**
     * Cập nhật trạng thái camera
     */
    CameraResponse updateCameraStatus(Long id, String status);
}
