package com.example.camerasurveillancesystem.ai.detector;

import ai.onnxruntime.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * YOLOv8 Detector sử dụng ONNX Runtime (thay vì OpenCV DNN)
 * Đây là implementation chính được khuyên dùng
 */
@Component
@Primary
@Slf4j
@RequiredArgsConstructor
public class YoloV8DetectorOnnxRuntime implements ObjectDetector {

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

    @Value("${ai.yolo.detection-filter.enable-filter:true}")
    private boolean enableFilter;

    @Value("${ai.yolo.detection-filter.allowed-classes:person,bicycle,car,motorcycle,bus,train,truck}")
    private String allowedClassesStr;

    private List<String> allowedClasses;

    private OrtEnvironment env;
    private OrtSession session;
    private boolean modelLoaded = false;

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
        // Parse allowed classes
        if (allowedClassesStr != null && !allowedClassesStr.isEmpty()) {
            allowedClasses = Arrays.asList(allowedClassesStr.split(","));
            log.info("✅ Detection filter enabled. Allowed classes: {}", allowedClasses);
        } else {
            allowedClasses = new ArrayList<>();
        }

        try {
            loadModel();
        } catch (Exception e) {
            log.warn("⚠️ YOLOv8 model not loaded. Detection features will be unavailable.");
            log.warn("📥 To enable AI detection, download model:");
            log.warn("   yolo export model=yolov8n.pt format=onnx");
            log.warn("   Then move yolov8n.onnx to: models/yolov8n.onnx");
        }
    }

    @Override
    public void loadModel() throws Exception {
        try {
            log.info("Loading YOLOv8 model with ONNX Runtime from: {}", modelPath);
            
            if (!Files.exists(Paths.get(modelPath))) {
                log.warn("⚠️ Model file not found. Auto-downloading...");
                boolean downloaded = modelDownloader.downloadModelIfNotExists(modelPath, modelVersion);
                
                if (!downloaded) {
                    log.warn("⚠️ Auto-download failed.");
                    log.warn("📥 Please export YOLOv8 model:");
                    log.warn("   yolo export model=yolov8n.pt format=onnx");
                    log.warn("   Then save to: {}", new File(modelPath).getAbsolutePath());
                    throw new RuntimeException("Model file not found: " + modelPath);
                }
            }
            
            // Create ONNX Runtime environment
            env = OrtEnvironment.getEnvironment();
            
            // Create session options
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);
            
            // Load model
            session = env.createSession(modelPath, opts);
            
            modelLoaded = true;
            log.info("✅ YOLOv8 model loaded successfully with ONNX Runtime");
            
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
            Mat resized = new Mat();
            Imgproc.resize(image, resized, new org.opencv.core.Size(inputSize, inputSize));
            
            // Convert BGR to RGB
            Mat rgb = new Mat();
            Imgproc.cvtColor(resized, rgb, Imgproc.COLOR_BGR2RGB);
            
            // Normalize and convert to float array [1, 3, 640, 640]
            float[] inputData = new float[1 * 3 * inputSize * inputSize];
            
            byte[] data = new byte[(int) rgb.total() * rgb.channels()];
            rgb.get(0, 0, data);
            
            int idx = 0;
            for (int c = 0; c < 3; c++) {
                for (int h = 0; h < inputSize; h++) {
                    for (int w = 0; w < inputSize; w++) {
                        int pixelIdx = (h * inputSize + w) * 3 + c;
                        inputData[idx++] = (data[pixelIdx] & 0xFF) / 255.0f;
                    }
                }
            }
            
            // Create tensor
            long[] shape = {1, 3, inputSize, inputSize};
            OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
            
            // Run inference
            Map<String, OnnxTensor> inputs = Map.of("images", tensor);
            OrtSession.Result results = session.run(inputs);
            
            // Post-processing
            float[][][] output = (float[][][]) results.get(0).getValue();
            List<DetectionResult> detections = postProcess(output, image.width(), image.height());
            
            tensor.close();
            results.close();
            
            return detections;
            
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

    private List<DetectionResult> postProcess(float[][][] output, int originalWidth, int originalHeight) {
        List<DetectionResult> results = new ArrayList<>();
        
        // YOLOv8 output: [1, 84, 8400] -> [batch, (4 bbox + 80 classes), predictions]
        int numDetections = output[0][0].length;
        
        List<float[]> boxes = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        List<Integer> classIds = new ArrayList<>();
        
        float scaleX = (float) originalWidth / inputSize;
        float scaleY = (float) originalHeight / inputSize;
        
        for (int i = 0; i < numDetections; i++) {
            // Get bbox coordinates
            float cx = output[0][0][i];
            float cy = output[0][1][i];
            float w = output[0][2][i];
            float h = output[0][3][i];
            
            // Find max class score
            float maxScore = 0;
            int maxClassId = -1;
            for (int c = 4; c < 84; c++) {
                float score = output[0][c][i];
                if (score > maxScore) {
                    maxScore = score;
                    maxClassId = c - 4;
                }
            }
            
            if (maxScore > confidenceThreshold) {
                // Apply class filter if enabled
                if (enableFilter && !allowedClasses.isEmpty()) {
                    String className = CLASS_NAMES[maxClassId];
                    if (!allowedClasses.contains(className)) {
                        continue; // Skip this detection
                    }
                }

                float x = (cx - w / 2) * scaleX;
                float y = (cy - h / 2) * scaleY;
                w *= scaleX;
                h *= scaleY;
                
                boxes.add(new float[]{x, y, w, h});
                confidences.add(maxScore);
                classIds.add(maxClassId);
            }
        }
        
        // Apply NMS
        List<Integer> indices = nms(boxes, confidences, nmsThreshold);
        
        for (int idx : indices) {
            float[] box = boxes.get(idx);
            
            DetectionResult result = DetectionResult.builder()
                .objectType(mapClassToType(classIds.get(idx)))
                .confidence((double) confidences.get(idx))
                .label(CLASS_NAMES[classIds.get(idx)])
                .boundingBox(DetectionResult.BoundingBox.builder()
                    .x((int) box[0])
                    .y((int) box[1])
                    .width((int) box[2])
                    .height((int) box[3])
                    .build())
                .build();
            
            results.add(result);
        }
        
        log.info("Detected {} objects", results.size());
        return results;
    }
    
    private List<Integer> nms(List<float[]> boxes, List<Float> confidences, float threshold) {
        List<Integer> indices = new ArrayList<>();
        List<Integer> sorted = new ArrayList<>();
        
        for (int i = 0; i < confidences.size(); i++) {
            sorted.add(i);
        }
        sorted.sort((a, b) -> Float.compare(confidences.get(b), confidences.get(a)));
        
        while (!sorted.isEmpty()) {
            int best = sorted.get(0);
            indices.add(best);
            sorted.remove(0);
            
            sorted.removeIf(i -> {
                float iou = calculateIoU(boxes.get(best), boxes.get(i));
                return iou > threshold;
            });
        }
        
        return indices;
    }
    
    private float calculateIoU(float[] box1, float[] box2) {
        float x1 = Math.max(box1[0], box2[0]);
        float y1 = Math.max(box1[1], box2[1]);
        float x2 = Math.min(box1[0] + box1[2], box2[0] + box2[2]);
        float y2 = Math.min(box1[1] + box1[3], box2[1] + box2[3]);
        
        float intersection = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        float area1 = box1[2] * box1[3];
        float area2 = box2[2] * box2[3];
        float union = area1 + area2 - intersection;
        
        return intersection / union;
    }

    private String mapClassToType(int classId) {
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
        try {
            if (session != null) {
                session.close();
            }
            modelLoaded = false;
            log.info("YOLOv8 model unloaded");
        } catch (Exception e) {
            log.error("Error unloading model", e);
        }
    }
}
