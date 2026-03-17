package com.example.camerasurveillancesystem.service;

import com.example.camerasurveillancesystem.ai.detector.DetectionResult;
import com.example.camerasurveillancesystem.dto.request.StreamAutoAlertConfigRequest;
import com.example.camerasurveillancesystem.dto.response.StreamAutoAlertConfigResponse;

import java.util.List;

/**
 * Service để xử lý AI detection từ video stream
 */
public interface AiDetectionStreamService {

    /**
     * Bắt đầu AI detection cho một camera
     * 
     * @param cameraId ID của camera
     * @return true nếu bắt đầu thành công
     */
    boolean startDetection(Long cameraId);

    /**
     * Dừng AI detection cho một camera
     * 
     * @param cameraId ID của camera
     */
    void stopDetection(Long cameraId);

    /**
     * Bắt đầu detection cho tất cả cameras active
     * 
     * @return số lượng cameras đã bắt đầu detection
     */
    int startDetectionForAllActiveCameras();

    /**
     * Dừng tất cả detections
     */
    void stopAllDetections();

    /**
     * Kiểm tra detection có đang chạy không
     * 
     * @param cameraId ID của camera
     * @return true nếu đang chạy
     */
    boolean isDetectionRunning(Long cameraId);

    /**
     * Lấy thông tin detection gần nhất
     * 
     * @param cameraId ID của camera
     * @return Danh sách detection results
     */
    List<DetectionResult> getLatestDetections(Long cameraId);

    /**
     * Cấu hình confidence threshold cho detection
     * 
     * @param cameraId ID của camera
     * @param threshold Threshold (0.0 - 1.0)
     */
    void setConfidenceThreshold(Long cameraId, Double threshold);

    /**
     * Cấu hình frame skip (phát hiện mỗi N frames)
     * 
     * @param cameraId ID của camera
     * @param skipFrames Số frames skip giữa mỗi lần detection
     */
    void setFrameSkip(Long cameraId, int skipFrames);

    /**
     * Lấy lý do fail lần start detection gần nhất (nếu có).
     *
     * @param cameraId ID của camera
     * @return message lỗi hoặc null
     */
    String getLastStartError(Long cameraId);

    /**
     * Cấu hình tự sinh alert từ kết quả AI detection.
     */
    void configureAutoAlert(StreamAutoAlertConfigRequest request);

    /**
     * Lấy cấu hình tự sinh alert theo camera.
     */
    StreamAutoAlertConfigResponse getAutoAlertConfig(Long cameraId);
}
