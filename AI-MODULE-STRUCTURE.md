# 🤖 AI MODULE STRUCTURE

## Cấu trúc thư mục AI Module

```
ai/
├── config/
│   ├── YoloConfig.java              # Cấu hình YOLOv8
│   └── OpenCVConfig.java            # Cấu hình OpenCV
│
├── detector/
│   ├── YoloV8Detector.java          # YOLOv8 detector chính
│   ├── ObjectDetector.java          # Interface detector
│   └── DetectionResult.java         # Kết quả detection
│
├── service/
│   ├── AiModelService.java          # Quản lý AI models
│   ├── AiEventService.java          # Xử lý AI events
│   ├── ProcessingJobService.java    # Background jobs
│   ├── VideoAnalysisService.java    # Phân tích video
│   └── impl/
│       └── ...ServiceImpl.java
│
├── controller/
│   ├── AiModelController.java
│   ├── AiEventController.java
│   └── ProcessingJobController.java
│
└── dto/
    ├── DetectionRequest.java
    ├── DetectionResponse.java
    └── ...

```

## Dependencies cần thêm vào pom.xml

```xml
<!-- YOLOv8 with DJL (Deep Java Library) -->
<dependency>
    <groupId>ai.djl</groupId>
    <artifactId>api</artifactId>
    <version>0.26.0</version>
</dependency>
<dependency>
    <groupId>ai.djl.pytorch</groupId>
    <artifactId>pytorch-engine</artifactId>
    <version>0.26.0</version>
</dependency>

<!-- OpenCV -->
<dependency>
    <groupId>org.openpnp</groupId>
    <artifactId>opencv</artifactId>
    <version>4.7.0-0</version>
</dependency>

<!-- Image processing -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.9</version>
</dependency>
```

## Workflow

1. **Camera Stream** → Frame extraction
2. **Frame** → YOLOv8 Detection
3. **Detection** → Create AiEvent + AiEventObjects
4. **AiEvent** → Trigger Alert (if threshold met)
5. **Alert** → Send Notifications
