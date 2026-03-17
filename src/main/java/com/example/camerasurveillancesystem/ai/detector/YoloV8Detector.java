package com.example.camerasurveillancesystem.ai.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * YOLOv8 Detector sử dụng OpenCV DNN
 * ⚠️ KHÔNG HOẠT ĐỘNG với YOLOv8 do DFL head không tương thích
 * → Dùng YoloV8DetectorOnnxRuntime thay thế
 */
@Component("yoloV8DetectorOpenCV")
@Slf4j
@RequiredArgsConstructor
public class YoloV8Detector implements ObjectDetector {

    private final ModelDownloader modelDownloader;

    @Value("${ai.yolo.model.path:models/yolov8n.onnx}")
    private String modelPath;

    @Value("${ai.yolo.model.version:8n}")
    private String modelVersion;

    @Value("${ai.yolo.confidence.threshold:0.5}")
    private float confidenceThreshold;

    @Value("${ai.yolo.nms.threshold:0.4}")
    private float nmsThreshold;

    @Value("${ai.yolo.input.size:640}")
    private int inputSize;

    private Net yoloNet;
    private boolean modelLoaded = false;

    // COCO class names (80 classes)
    private static final String[] CLASS_NAMES = {
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
        "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
        "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
        "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
        "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
        "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
        "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
        "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    };

    @PostConstruct
    public void init() {
        try {
            loadModel();
        } catch (Exception e) {
            log.warn("⚠️ YOLOv8 model not loaded. Detection features will be unavailable.");
            log.warn("📥 To enable AI detection:");
            log.warn("   pip install ultralytics");
            log.warn("   yolo export model=yolov8n.pt format=onnx");
            log.warn("   Then move yolov8n.onnx to: models/yolov8n.onnx");
        }
    }

    @Override
    public void loadModel() throws Exception {
        try {
            log.info("Loading YOLOv8 model from: {}", modelPath);
            
            // ✅ Tự động download nếu chưa có
            if (!Files.exists(Paths.get(modelPath))) {
                log.warn("⚠️ Model file not found. Auto-downloading...");
                
                boolean downloaded = modelDownloader.downloadModelIfNotExists(modelPath, modelVersion);
                
                if (!downloaded) {
                    log.warn("⚠️ Auto-download failed.");
                    log.warn("📥 Please download YOLOv8n model manually:");
                    log.warn("   Method 1 (Python):");
                    log.warn("      pip install ultralytics");
                    log.warn("      yolo export model=yolov8n.pt format=onnx");
                    log.warn("   Method 2 (Direct):");
                    log.warn("      Visit: https://github.com/ultralytics/ultralytics");
                    log.warn("   Then save file to: {}", new File(modelPath).getAbsolutePath());
                    throw new RuntimeException("Model file not found: " + modelPath);
                }
                
                log.info("✅ Model downloaded successfully!");
            }
            
            // Load model với OpenCV DNN
            yoloNet = Dnn.readNetFromONNX(modelPath);
            
            // Set backend và target
            yoloNet.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
            yoloNet.setPreferableTarget(Dnn.DNN_TARGET_CPU);
            // Nếu có GPU: DNN_TARGET_CUDA
            
            modelLoaded = true;
            log.info("✅ YOLOv8 model loaded successfully");
        } catch (Exception e) {
            modelLoaded = false;
            log.error("❌ Failed to load YOLOv8 model", e);
            throw e;
        }
    }

    @Override
    public List<DetectionResult> detect(Mat image) {
        if (!modelLoaded) {
            log.error("Model not loaded");
            return new ArrayList<>();
        }

        try {
            // Preprocessing
            Mat blob = Dnn.blobFromImage(
                image,
                1.0 / 255.0,                    // Scale
                new Size(inputSize, inputSize), // Size
                new Scalar(0, 0, 0),           // Mean
                true,                           // Swap RB
                false                           // Crop
            );

            // Forward pass
            yoloNet.setInput(blob);
            Mat output = yoloNet.forward();

            // Post-processing
            return postProcess(output, image.width(), image.height());

        } catch (Exception e) {
            log.error("Error during detection", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DetectionResult> detect(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            log.error("Failed to load image from: {}", imagePath);
            return new ArrayList<>();
        }
        return detect(image);
    }

    @Override
    public List<DetectionResult> detect(byte[] imageBytes) {
        MatOfByte mob = new MatOfByte(imageBytes);
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            log.error("Failed to decode image from bytes");
            return new ArrayList<>();
        }
        return detect(image);
    }

    private List<DetectionResult> postProcess(Mat output, int originalWidth, int originalHeight) {
        List<DetectionResult> results = new ArrayList<>();
        
        // YOLOv8 output format: [batch, 84, 8400]
        // 84 = 4 bbox coords + 80 class scores
        
        float[] data = new float[(int) (output.total() * output.channels())];
        output.get(0, 0, data);

        int rows = output.size(1); // 84
        int cols = output.size(2); // 8400

        List<Rect2d> boxes = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        List<Integer> classIds = new ArrayList<>();

        float scaleX = (float) originalWidth / inputSize;
        float scaleY = (float) originalHeight / inputSize;

        // Parse detections
        for (int i = 0; i < cols; i++) {
            int offset = i * rows;
            
            // Get bbox
            float cx = data[offset];
            float cy = data[offset + 1];
            float w = data[offset + 2];
            float h = data[offset + 3];

            // Get max class score
            float maxScore = 0;
            int maxClassId = -1;
            for (int c = 4; c < rows; c++) {
                float score = data[offset + c];
                if (score > maxScore) {
                    maxScore = score;
                    maxClassId = c - 4;
                }
            }

            if (maxScore > confidenceThreshold) {
                // Convert from center to corner format
                float x = (cx - w / 2) * scaleX;
                float y = (cy - h / 2) * scaleY;
                w *= scaleX;
                h *= scaleY;

                boxes.add(new Rect2d(x, y, w, h));
                confidences.add(maxScore);
                classIds.add(maxClassId);
            }
        }

        // Apply NMS
        MatOfRect2d boxesMat = new MatOfRect2d();
        boxesMat.fromList(boxes);
        
        MatOfFloat confMat = new MatOfFloat();
        confMat.fromList(confidences);
        
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxes(boxesMat, confMat, confidenceThreshold, nmsThreshold, indices);

        // Build results
        int[] indicesArray = indices.toArray();
        for (int idx : indicesArray) {
            Rect2d box = boxes.get(idx);
            
            DetectionResult result = DetectionResult.builder()
                .objectType(mapClassToType(classIds.get(idx)))
                .confidence((double) confidences.get(idx))
                .label(CLASS_NAMES[classIds.get(idx)])
                .boundingBox(DetectionResult.BoundingBox.builder()
                    .x((int) box.x)
                    .y((int) box.y)
                    .width((int) box.width)
                    .height((int) box.height)
                    .build())
                .build();
            
            results.add(result);
        }

        log.info("Detected {} objects", results.size());
        return results;
    }

    private String mapClassToType(int classId) {
        // Map COCO classes to our object types
        String className = CLASS_NAMES[classId];
        
        if (className.equals("person")) return "PERSON";
        if (Arrays.asList("car", "truck", "bus", "motorcycle").contains(className)) return "VEHICLE";
        if (Arrays.asList("cat", "dog", "bird", "horse").contains(className)) return "ANIMAL";
        
        return "OBJECT";
    }

    @Override
    public boolean isModelLoaded() {
        return modelLoaded;
    }

    @Override
    public void unloadModel() {
        if (yoloNet != null) {
            yoloNet = null;
        }
        modelLoaded = false;
        log.info("YOLOv8 model unloaded");
    }
}
