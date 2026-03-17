package com.example.camerasurveillancesystem.service;

/**
 * Service để tương tác với MediaMTX qua REST API.
 * Tự động đăng ký/xóa RTSP path khi camera được tạo/xóa.
 */
public interface MediaMTXService {

    /**
     * Đăng ký một camera vào MediaMTX với RTSP source.
     * MediaMTX sẽ tự pull stream từ camera.
     *
     * @param cameraCode mã camera, dùng làm path trên MediaMTX (vd: CAM-001)
     * @param rtspUrl    RTSP URL của camera thật (vd: rtsp://admin:pass@192.168.1.100:554/stream1)
     * @return true nếu đăng ký thành công
     */
    boolean registerCamera(String cameraCode, String rtspUrl);

    /**
     * Xóa path của camera khỏi MediaMTX.
     *
     * @param cameraCode mã camera
     */
    void unregisterCamera(String cameraCode);

    /**
     * Cập nhật RTSP source khi camera đổi URL.
     *
     * @param cameraCode mã camera
     * @param newRtspUrl RTSP URL mới
     * @return true nếu cập nhật thành công
     */
    boolean updateCamera(String cameraCode, String newRtspUrl);

    /**
     * Trả về HLS stream URL cho frontend phát video.
     * Pattern: {mediamtx.hls.url}/{cameraCode}/index.m3u8
     *
     * @param cameraCode mã camera
     * @return HLS URL string
     */
    String getHlsUrl(String cameraCode);

    /**
     * Trả về WebRTC stream URL cho frontend phát low-latency.
     *
     * @param cameraCode mã camera
     * @return WebRTC URL string
     */
    String getWebRtcUrl(String cameraCode);

    /**
     * Trả về RTSP relay URL từ MediaMTX cho backend đọc stream ổn định hơn.
     * Pattern: {mediamtx.rtsp.url}/{cameraCode}
     *
     * @param cameraCode mã camera
     * @return RTSP relay URL
     */
    String getRtspUrl(String cameraCode);

    /**
     * Kiểm tra MediaMTX integration có bật hay không.
     */
    boolean isEnabled();
}
