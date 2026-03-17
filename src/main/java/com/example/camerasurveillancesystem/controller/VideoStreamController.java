package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.VideoStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.Map;

/**
 * Controller cho video streaming endpoints
 * Frontend có thể dùng để hiển thị live stream từ camera
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
@Tag(name = "Video Stream", description = "APIs for video streaming to frontend")
public class VideoStreamController {

    private final VideoStreamService videoStreamService;
    private final CameraRepository cameraRepository;

    /**
     * Lấy snapshot (frame hiện tại) từ camera
     * Frontend dùng để hiển thị ảnh tĩnh hoặc polling
     * Stream tự động khởi động nếu chưa chạy.
     *
     * Usage: <img src="/api/v1/video/camera/1/snapshot" />
     */
    @GetMapping("/camera/{cameraId}/snapshot")
    @Operation(summary = "Get current frame snapshot from camera")
    public ResponseEntity<byte[]> getSnapshot(@PathVariable Long cameraId) {
        try {
            // Tự động start stream nếu chưa chạy
            if (!videoStreamService.isStreamRunning(cameraId)) {
                Camera camera = cameraRepository.findById(cameraId).orElse(null);
                if (camera == null) {
                    return ResponseEntity.notFound().build();
                }
                if (camera.getRtspUrl() != null && !camera.getRtspUrl().isBlank()) {
                    log.info("Auto-starting stream for snapshot, camera {}", cameraId);
                    videoStreamService.startStream(cameraId, camera.getRtspUrl(), null);
                    // Đợi 1 frame đầu tiên
                    Thread.sleep(500);
                }
            }

            Mat frame = videoStreamService.getCurrentFrame(cameraId);
            
            if (frame == null || frame.empty()) {
                log.warn("No frame available for camera {}", cameraId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            // Convert Mat to JPEG bytes
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, buffer);
            byte[] imageBytes = buffer.toArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                .headers(headers)
                .body(imageBytes);

        } catch (Exception e) {
            log.error("Error getting snapshot for camera {}: {}", cameraId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * MJPEG stream endpoint - continuous stream of JPEG frames
     * Frontend dùng để hiển thị live video
     * Stream tự động khởi động khi được gọi, không cần bật detection trước.
     *
     * Usage: <img src="/api/v1/video/camera/1/stream" />
     */
    @GetMapping("/camera/{cameraId}/stream")
    @Operation(summary = "Get MJPEG stream from camera")
    public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable Long cameraId) {
        Camera camera = cameraRepository.findById(cameraId).orElse(null);

        if (camera == null) {
            return ResponseEntity.notFound().build();
        }

        // Tự động start stream nếu chưa chạy
        if (!videoStreamService.isStreamRunning(cameraId)) {
            if (camera.getRtspUrl() == null || camera.getRtspUrl().isBlank()) {
                log.warn("Camera {} has no RTSP URL configured", cameraId);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            log.info("Auto-starting stream for camera {} with URL: {}", cameraId, camera.getRtspUrl());
            boolean started = videoStreamService.startStream(cameraId, camera.getRtspUrl(), null);
            if (!started) {
                log.error("Failed to start stream for camera {}", cameraId);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }

        StreamingResponseBody stream = outputStream -> {
            try {
                streamMjpeg(cameraId, outputStream);
            } catch (Exception e) {
                log.error("Error streaming from camera {}: {}", cameraId, e.getMessage());
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/x-mixed-replace; boundary=frame"));
        headers.setCacheControl("no-cache");

        return ResponseEntity.ok()
            .headers(headers)
            .body(stream);
    }

    /**
     * MJPEG streaming implementation
     */
    private void streamMjpeg(Long cameraId, OutputStream outputStream) throws Exception {
        while (videoStreamService.isStreamRunning(cameraId)) {
            Mat frame = videoStreamService.getCurrentFrame(cameraId);
            
            if (frame != null && !frame.empty()) {
                // Convert to JPEG
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, buffer);
                byte[] imageBytes = buffer.toArray();

                // Write MJPEG frame
                outputStream.write(("--frame\r\n").getBytes());
                outputStream.write(("Content-Type: image/jpeg\r\n").getBytes());
                outputStream.write(("Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
                outputStream.write(imageBytes);
                outputStream.write("\r\n".getBytes());
                outputStream.flush();
            }

            // Control frame rate (~30 FPS)
            Thread.sleep(33);
        }
    }

    /**
     * Kiểm tra camera stream status
     */
    @GetMapping("/camera/{cameraId}/status")
    @Operation(summary = "Check if camera stream is active")
    public ResponseEntity<?> getStreamStatus(@PathVariable Long cameraId) {
        boolean isRunning = videoStreamService.isStreamRunning(cameraId);
        
        return ResponseEntity.ok(Map.of(
            "cameraId", cameraId,
            "streamActive", isRunning,
            "timestamp", System.currentTimeMillis()
        ));
    }
}
