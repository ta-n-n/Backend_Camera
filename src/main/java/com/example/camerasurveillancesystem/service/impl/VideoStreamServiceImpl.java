package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.service.VideoStreamService;
import com.example.camerasurveillancesystem.service.RtspConnectionTestResult;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Slf4j
@Service
public class VideoStreamServiceImpl implements VideoStreamService {

    private final Map<Long, StreamContext> activeStreams = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final LinkedHashMap<Integer, String> RTSP_BACKENDS = new LinkedHashMap<>();

    static {
        RTSP_BACKENDS.put(Videoio.CAP_FFMPEG, "CAP_FFMPEG");
        RTSP_BACKENDS.put(Videoio.CAP_GSTREAMER, "CAP_GSTREAMER");
        RTSP_BACKENDS.put(Videoio.CAP_MSMF, "CAP_MSMF");
        RTSP_BACKENDS.put(Videoio.CAP_DSHOW, "CAP_DSHOW");
    }

    private boolean openCaptureWithFallback(VideoCapture capture, String url) {
        // Try known RTSP-capable backends explicitly, avoid CAP_ANY since it can
        // route to CAP_IMAGES and treat RTSP URL as file pattern.
        for (Map.Entry<Integer, String> backend : RTSP_BACKENDS.entrySet()) {
            int backendId = backend.getKey();
            String backendName = backend.getValue();

            boolean opened = false;
            try {
                opened = capture.open(url, backendId) && capture.isOpened();
            } catch (Exception e) {
                log.debug("Backend {} threw while opening {}: {}", backendName, url, e.getMessage());
            }

            if (opened) {
                log.info("Opened RTSP with {}: {}", backendName, url);
                return true;
            }

            if (capture.isOpened()) {
                capture.release();
            }
        }

        log.warn("Failed to open RTSP URL with all configured backends: {}", url);
        return false;
    }

    private FFmpegFrameGrabber createAndStartGrabber(String url, int timeoutMs) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
        try {
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("stimeout", String.valueOf((long) timeoutMs * 1000));
            grabber.setOption("rw_timeout", String.valueOf((long) timeoutMs * 1000));
            grabber.setOption("fflags", "nobuffer");
            grabber.start();
            log.info("Opened RTSP with FFmpegFrameGrabber: {}", url);
            return grabber;
        } catch (Exception e) {
            try {
                grabber.stop();
            } catch (Exception ignore) {
            }
            try {
                grabber.release();
            } catch (Exception ignore) {
            }
            log.debug("FFmpegFrameGrabber failed for {}: {}", url, e.getMessage());
            return null;
        }
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        if (image == null) {
            return null;
        }

        BufferedImage bgrImage = image;
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            bgrImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bgrImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }

        byte[] pixels = ((DataBufferByte) bgrImage.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bgrImage.getHeight(), bgrImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    private List<String> buildRtspCandidateUrls(String rtspUrl) {
        List<String> candidates = new ArrayList<>();
        candidates.add(rtspUrl);

        if (rtspUrl.startsWith("rtsp://") && !rtspUrl.contains("rtsp_transport")) {
            String tcpUrl = rtspUrl.contains("?")
                    ? rtspUrl + "&rtsp_transport=tcp"
                    : rtspUrl + "?rtsp_transport=tcp";
            candidates.add(tcpUrl);
        }

        return candidates;
    }

    @Override
    public boolean startStream(Long cameraId, String rtspUrl, Consumer<Mat> frameProcessor) {
        log.info("Starting stream for camera {} with URL: {}", cameraId, rtspUrl);

        // Dừng stream cũ nếu có
        if (isStreamRunning(cameraId)) {
            log.warn("Stream already running for camera {}, stopping old stream", cameraId);
            stopStream(cameraId);
        }

        try {
            String normalizedRtspUrl = rtspUrl != null ? rtspUrl.trim() : null;
            if (normalizedRtspUrl == null || normalizedRtspUrl.isBlank()) {
                log.error("Cannot start stream for camera {} due to empty RTSP URL", cameraId);
                return false;
            }

            VideoCapture capture = null;
            FFmpegFrameGrabber grabber = null;
            String selectedUrl = null;
            List<String> candidateUrls = buildRtspCandidateUrls(normalizedRtspUrl);

            for (String candidateUrl : candidateUrls) {
                capture = new VideoCapture();

                // Cấu hình VideoCapture cho RTSP stream
                capture.set(Videoio.CAP_PROP_BUFFERSIZE, 1); // Buffer nhỏ để giảm latency
                capture.set(Videoio.CAP_PROP_OPEN_TIMEOUT_MSEC, 10000); // Timeout 10s
                capture.set(Videoio.CAP_PROP_READ_TIMEOUT_MSEC, 5000);  // Read timeout 5s

                // Mở stream với backend fallback để tăng tương thích runtime
                openCaptureWithFallback(capture, candidateUrl);

                if (capture.isOpened()) {
                    selectedUrl = candidateUrl;
                    break;
                }

                capture.release();
                capture = null;
            }

            if (capture == null || !capture.isOpened()) {
                for (String candidateUrl : candidateUrls) {
                    grabber = createAndStartGrabber(candidateUrl, 10000);
                    if (grabber != null) {
                        selectedUrl = candidateUrl;
                        break;
                    }
                }

                if (grabber == null) {
                    log.error("Failed to open stream for camera {} after trying {} URL variants", cameraId, candidateUrls.size());
                    return false;
                }
            }

            log.info("Camera {} stream opened with URL: {}", cameraId, selectedUrl);

            StreamContext context = new StreamContext(cameraId, selectedUrl, capture, grabber, frameProcessor);
            activeStreams.put(cameraId, context);

            // Chạy stream processing trong background thread
            Future<?> future = executorService.submit(() -> {
                if (context.getGrabber() != null) {
                    processStreamWithGrabber(context);
                } else {
                    processStream(context);
                }
            });
            context.setFuture(future);

            log.info("Stream started successfully for camera {}", cameraId);
            return true;

        } catch (Exception e) {
            log.error("Error starting stream for camera {}: {}", cameraId, e.getMessage(), e);
            return false;
        }
    }

    private void processStream(StreamContext context) {
        log.info("Processing stream for camera {}", context.getCameraId());
        
        Mat frame = new Mat();
        int errorCount = 0;
        int maxErrors = 10;

        while (context.isRunning()) {
            try {
                if (context.getCapture().read(frame)) {
                    if (!frame.empty()) {
                        // Clone frame để tránh concurrent modification
                        Mat clonedFrame = frame.clone();
                        
                        // Lưu frame hiện tại
                        context.setCurrentFrame(clonedFrame);
                        
                        // Gọi frame processor
                        if (context.getFrameProcessor() != null) {
                            context.getFrameProcessor().accept(clonedFrame);
                        }
                        
                        errorCount = 0; // Reset error count khi đọc frame thành công
                    }
                } else {
                    errorCount++;
                    log.warn("Failed to read frame from camera {}, error count: {}", 
                            context.getCameraId(), errorCount);
                    
                    if (errorCount >= maxErrors) {
                        log.error("Too many errors reading from camera {}, stopping stream", 
                                context.getCameraId());
                        break;
                    }
                    
                    // Đợi một chút trước khi thử lại
                    Thread.sleep(1000);
                }

                // Giảm CPU usage
                Thread.sleep(33); // ~30 FPS

            } catch (InterruptedException e) {
                log.info("Stream processing interrupted for camera {}", context.getCameraId());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing stream for camera {}: {}", 
                        context.getCameraId(), e.getMessage(), e);
                errorCount++;
                
                if (errorCount >= maxErrors) {
                    break;
                }
            }
        }

        // Cleanup
        frame.release();
        if (context.getCapture() != null && context.getCapture().isOpened()) {
            context.getCapture().release();
        }
        context.setRunning(false);
        
        log.info("Stream processing stopped for camera {}", context.getCameraId());
    }

    private void processStreamWithGrabber(StreamContext context) {
        log.info("Processing stream with FFmpegFrameGrabber for camera {}", context.getCameraId());

        Java2DFrameConverter converter = new Java2DFrameConverter();
        int errorCount = 0;
        int maxErrors = 10;

        while (context.isRunning()) {
            try {
                Frame frame = context.getGrabber().grabImage();
                if (frame != null) {
                    BufferedImage bufferedImage = converter.getBufferedImage(frame);
                    Mat mat = bufferedImageToMat(bufferedImage);

                    if (mat != null && !mat.empty()) {
                        Mat clonedFrame = mat.clone();
                        context.setCurrentFrame(clonedFrame);

                        if (context.getFrameProcessor() != null) {
                            context.getFrameProcessor().accept(clonedFrame);
                        }

                        mat.release();
                        errorCount = 0;
                    } else {
                        errorCount++;
                    }
                } else {
                    errorCount++;
                }

                if (errorCount >= maxErrors) {
                    log.error("Too many errors reading from camera {} via FFmpegFrameGrabber", context.getCameraId());
                    break;
                }

                Thread.sleep(33);
            } catch (InterruptedException e) {
                log.info("FFmpegFrameGrabber processing interrupted for camera {}", context.getCameraId());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                errorCount++;
                log.error("Error processing FFmpegFrameGrabber stream for camera {}: {}", context.getCameraId(), e.getMessage(), e);
                if (errorCount >= maxErrors) {
                    break;
                }
            }
        }

        try {
            if (context.getGrabber() != null) {
                context.getGrabber().stop();
                context.getGrabber().release();
            }
        } catch (Exception e) {
            log.warn("Error releasing FFmpegFrameGrabber for camera {}: {}", context.getCameraId(), e.getMessage());
        }

        context.setRunning(false);
        log.info("FFmpegFrameGrabber stream processing stopped for camera {}", context.getCameraId());
    }

    @Override
    public void stopStream(Long cameraId) {
        log.info("Stopping stream for camera {}", cameraId);
        
        StreamContext context = activeStreams.remove(cameraId);
        if (context != null) {
            context.setRunning(false);
            
            // Cancel future task
            if (context.getFuture() != null) {
                context.getFuture().cancel(true);
            }
            
            // Release resources
            if (context.getCurrentFrame() != null) {
                context.getCurrentFrame().release();
            }

            if (context.getCapture() != null && context.getCapture().isOpened()) {
                context.getCapture().release();
            }

            if (context.getGrabber() != null) {
                try {
                    context.getGrabber().stop();
                } catch (Exception ignore) {
                }
                try {
                    context.getGrabber().release();
                } catch (Exception ignore) {
                }
            }
            
            log.info("Stream stopped for camera {}", cameraId);
        } else {
            log.warn("No active stream found for camera {}", cameraId);
        }
    }

    @Override
    public void stopAllStreams() {
        log.info("Stopping all streams");
        
        activeStreams.keySet().forEach(this::stopStream);
        
        log.info("All streams stopped");
    }

    @Override
    public boolean isStreamRunning(Long cameraId) {
        StreamContext context = activeStreams.get(cameraId);
        return context != null && context.isRunning();
    }

    @Override
    public Mat getCurrentFrame(Long cameraId) {
        StreamContext context = activeStreams.get(cameraId);
        if (context != null && context.getCurrentFrame() != null) {
            return context.getCurrentFrame().clone();
        }
        return null;
    }

    @Override
    public boolean restartStream(Long cameraId) {
        log.info("Restarting stream for camera {}", cameraId);
        
        StreamContext context = activeStreams.get(cameraId);
        if (context != null) {
            String rtspUrl = context.getRtspUrl();
            Consumer<Mat> frameProcessor = context.getFrameProcessor();
            
            stopStream(cameraId);
            
            // Đợi một chút trước khi restart
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return startStream(cameraId, rtspUrl, frameProcessor);
        }
        
        log.warn("Cannot restart stream for camera {}, no context found", cameraId);
        return false;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up VideoStreamService");
        stopAllStreams();
        executorService.shutdown();
    }

    @Override
    public boolean testRtspConnection(String rtspUrl, int timeoutSeconds) {
        return testRtspConnectionDetailed(rtspUrl, timeoutSeconds).isSuccess();
    }

    @Override
    public RtspConnectionTestResult testRtspConnectionDetailed(String rtspUrl, int timeoutSeconds) {
        log.info("Testing RTSP connection: {}", rtspUrl);

        String normalizedRtspUrl = rtspUrl != null ? rtspUrl.trim() : null;
        if (normalizedRtspUrl == null || normalizedRtspUrl.isBlank()) {
            return RtspConnectionTestResult.builder()
                    .success(false)
                    .diagnosticCode("INVALID_RTSP_URL")
                    .diagnosticMessage("RTSP URL trống hoặc không hợp lệ")
                    .normalizedRtspUrl(normalizedRtspUrl)
                    .build();
        }

        String selectedUrl = normalizedRtspUrl;
        boolean openedAtLeastOnce = false;
        boolean readFrameFailed = false;
        List<String> candidateUrls = buildRtspCandidateUrls(normalizedRtspUrl);

        try {
            for (String candidateUrl : candidateUrls) {
                selectedUrl = candidateUrl;
                VideoCapture capture = new VideoCapture();
                try {
                    capture.set(Videoio.CAP_PROP_OPEN_TIMEOUT_MSEC, (long) timeoutSeconds * 1000);
                    capture.set(Videoio.CAP_PROP_READ_TIMEOUT_MSEC, (long) timeoutSeconds * 1000);

                    openCaptureWithFallback(capture, candidateUrl);
                    if (!capture.isOpened()) {
                        continue;
                    }

                    openedAtLeastOnce = true;

                    // Đọc thử 1 frame để chắc chắn có video
                    Mat frame = new Mat();
                    boolean gotFrame = capture.read(frame);
                    boolean isEmpty = frame.empty();
                    frame.release();

                    if (gotFrame && !isEmpty) {
                        log.info("RTSP test OK with URL: {}", candidateUrl);
                        return RtspConnectionTestResult.builder()
                                .success(true)
                                .diagnosticCode("OK")
                                .diagnosticMessage("Kết nối RTSP thành công")
                                .normalizedRtspUrl(candidateUrl)
                                .build();
                    }

                    readFrameFailed = true;
                } finally {
                    if (capture.isOpened()) {
                        capture.release();
                    }
                }
            }

            if (!openedAtLeastOnce && !readFrameFailed) {
                for (String candidateUrl : candidateUrls) {
                    selectedUrl = candidateUrl;
                    FFmpegFrameGrabber grabber = createAndStartGrabber(candidateUrl, timeoutSeconds * 1000);
                    if (grabber == null) {
                        continue;
                    }

                    openedAtLeastOnce = true;
                    try {
                        Frame frame = grabber.grabImage();
                        if (frame != null) {
                            log.info("RTSP test OK with URL via FFmpegFrameGrabber: {}", candidateUrl);
                            return RtspConnectionTestResult.builder()
                                    .success(true)
                                    .diagnosticCode("OK")
                                    .diagnosticMessage("Kết nối RTSP thành công")
                                    .normalizedRtspUrl(candidateUrl)
                                    .build();
                        }
                        readFrameFailed = true;
                    } finally {
                        try {
                            grabber.stop();
                        } catch (Exception ignore) {
                        }
                        try {
                            grabber.release();
                        } catch (Exception ignore) {
                        }
                    }
                }
            }

            if (readFrameFailed || openedAtLeastOnce) {
                log.warn("RTSP test - opened but no frame: {}", normalizedRtspUrl);
                return RtspConnectionTestResult.builder()
                        .success(false)
                        .diagnosticCode("READ_FRAME_FAILED")
                        .diagnosticMessage("Mở được RTSP nhưng không đọc được frame (thường do sai profile stream hoặc codec không tương thích)")
                        .normalizedRtspUrl(selectedUrl)
                        .build();
            }

            log.warn("RTSP test failed - cannot open with all URL variants: {}", normalizedRtspUrl);
            return RtspConnectionTestResult.builder()
                    .success(false)
                    .diagnosticCode("OPEN_STREAM_FAILED")
                    .diagnosticMessage("Không thể mở kết nối RTSP (sai stream path, tài khoản, hoặc camera chưa bật RTSP)")
                    .normalizedRtspUrl(selectedUrl)
                    .build();

        } catch (Exception e) {
            log.error("RTSP test exception for {}: {}", normalizedRtspUrl, e.getMessage());
            return RtspConnectionTestResult.builder()
                    .success(false)
                    .diagnosticCode("RTSP_EXCEPTION")
                    .diagnosticMessage("Lỗi khi test RTSP: " + e.getMessage())
                    .normalizedRtspUrl(selectedUrl)
                    .build();
        }
    }

    /**
     * Context class để lưu thông tin về stream đang chạy
     */
    private static class StreamContext {
        private final Long cameraId;
        private final String rtspUrl;
        private final VideoCapture capture;
        private final FFmpegFrameGrabber grabber;
        private final Consumer<Mat> frameProcessor;
        private volatile boolean running;
        private volatile Mat currentFrame;
        private Future<?> future;

        public StreamContext(Long cameraId, String rtspUrl, VideoCapture capture,
                           FFmpegFrameGrabber grabber,
                           Consumer<Mat> frameProcessor) {
            this.cameraId = cameraId;
            this.rtspUrl = rtspUrl;
            this.capture = capture;
            this.grabber = grabber;
            this.frameProcessor = frameProcessor;
            this.running = true;
        }

        public Long getCameraId() {
            return cameraId;
        }

        public String getRtspUrl() {
            return rtspUrl;
        }

        public VideoCapture getCapture() {
            return capture;
        }

        public FFmpegFrameGrabber getGrabber() {
            return grabber;
        }

        public Consumer<Mat> getFrameProcessor() {
            return frameProcessor;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public Mat getCurrentFrame() {
            return currentFrame;
        }

        public void setCurrentFrame(Mat currentFrame) {
            // Release old frame
            if (this.currentFrame != null) {
                this.currentFrame.release();
            }
            this.currentFrame = currentFrame;
        }

        public Future<?> getFuture() {
            return future;
        }

        public void setFuture(Future<?> future) {
            this.future = future;
        }
    }
}
