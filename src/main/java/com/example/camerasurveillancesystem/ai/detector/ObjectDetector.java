package com.example.camerasurveillancesystem.ai.detector;

import org.opencv.core.Mat;

import java.util.List;

/**
 * Interface cho object detector
 */
public interface ObjectDetector {

    /**
     * Phát hiện đối tượng trong ảnh
     * 
     * @param image OpenCV Mat image
     * @return Danh sách đối tượng phát hiện được
     */
    List<DetectionResult> detect(Mat image);

    /**
     * Phát hiện đối tượng từ file ảnh
     * 
     * @param imagePath Đường dẫn file ảnh
     * @return Danh sách đối tượng phát hiện được
     */
    List<DetectionResult> detect(String imagePath);

    /**
     * Phát hiện đối tượng từ byte array
     * 
     * @param imageBytes Byte array của ảnh
     * @return Danh sách đối tượng phát hiện được
     */
    List<DetectionResult> detect(byte[] imageBytes);

    /**
     * Kiểm tra model đã load chưa
     */
    boolean isModelLoaded();

    /**
     * Load model
     */
    void loadModel() throws Exception;

    /**
     * Unload model
     */
    void unloadModel();
}
