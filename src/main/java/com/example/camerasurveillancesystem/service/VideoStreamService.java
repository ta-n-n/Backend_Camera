package com.example.camerasurveillancesystem.service;

import org.opencv.core.Mat;

import java.util.function.Consumer;

/**
 * Service để xử lý video stream từ RTSP
 */
public interface VideoStreamService {

    /**
     * Bắt đầu đọc stream từ RTSP URL
     * 
     * @param cameraId ID của camera
     * @param rtspUrl RTSP URL
     * @param frameProcessor Consumer để xử lý từng frame
     * @return true nếu stream khởi động thành công
     */
    boolean startStream(Long cameraId, String rtspUrl, Consumer<Mat> frameProcessor);

    /**
     * Dừng stream của một camera
     * 
     * @param cameraId ID của camera
     */
    void stopStream(Long cameraId);

    /**
     * Dừng tất cả streams
     */
    void stopAllStreams();

    /**
     * Kiểm tra stream có đang chạy không
     * 
     * @param cameraId ID của camera
     * @return true nếu stream đang chạy
     */
    boolean isStreamRunning(Long cameraId);

    /**
     * Lấy frame hiện tại từ stream
     * 
     * @param cameraId ID của camera
     * @return Mat frame hoặc null nếu không có
     */
    Mat getCurrentFrame(Long cameraId);

    /**
     * Restart stream khi bị disconnect
     * 
     * @param cameraId ID của camera
     * @return true nếu restart thành công
     */
    boolean restartStream(Long cameraId);

    /**
     * Kiểm tra kết nối RTSP URL có hợp lệ không (dùng trước khi lưu camera).
     *
     * @param rtspUrl       RTSP URL cần test
     * @param timeoutSeconds timeout tính bằng giây
     * @return true nếu kết nối OK
     */
    boolean testRtspConnection(String rtspUrl, int timeoutSeconds);

    /**
     * Kiểm tra RTSP và trả về chẩn đoán chi tiết.
     *
     * @param rtspUrl RTSP URL cần test
     * @param timeoutSeconds timeout tính bằng giây
     * @return kết quả chi tiết để debug
     */
    RtspConnectionTestResult testRtspConnectionDetailed(String rtspUrl, int timeoutSeconds);
}
