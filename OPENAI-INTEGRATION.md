# 🤖 OpenAI Integration (Optional)

## 📋 Tổng quan

Tích hợp OpenAI GPT-4 Vision để phân tích chi tiết hơn các frames/events từ camera:
- 📝 **Mô tả chi tiết scene** - Generate human-readable descriptions
- 🚨 **Phát hiện hành vi bất thường** - Detect suspicious activities
- 📊 **Phân tích context** - Hiểu ngữ cảnh của sự kiện
- 🔍 **Xác minh YOLOv8 detection** - Cross-validate với GPT-4 Vision

## ⚠️ Lưu ý

OpenAI integration là **OPTIONAL** và chỉ nên enable khi:
- Cần phân tích chi tiết hơn YOLOv8
- Có budget cho API calls ($0.01-0.02/image)
- Cần human-readable descriptions

**YOLOv8 đã đủ tốt** cho hầu hết use cases về detection người và xe cộ!

---

## 🛠️ Setup

### 1. Thêm dependency vào pom.xml

```xml
<!-- OpenAI Java SDK -->
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>

<!-- Hoặc dùng Spring AI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>0.8.1</version>
</dependency>
```

### 2. Cấu hình trong application.properties

```properties
# ================== OPENAI CONFIGURATION ==================
# Enable/Disable OpenAI integration
ai.openai.enabled=false

# API Key (Get from https://platform.openai.com/api-keys)
ai.openai.api-key=${OPENAI_API_KEY}

# Model configuration
ai.openai.model=gpt-4-vision-preview
ai.openai.max-tokens=300
ai.openai.temperature=0.7

# When to use OpenAI
ai.openai.use-for-verification=false
ai.openai.use-for-description=true
ai.openai.use-for-anomaly-detection=true

# Filtering
ai.openai.min-confidence-trigger=0.8
ai.openai.process-every-n-frames=30
```

### 3. Set environment variable

```bash
# Windows
set OPENAI_API_KEY=sk-your-api-key-here

# Linux/Mac
export OPENAI_API_KEY=sk-your-api-key-here
```

---

## 💻 Implementation

### 1. Tạo OpenAI Service

```java
// src/main/java/com/example/camerasurveillancesystem/ai/service/OpenAIService.java

package com.example.camerasurveillancesystem.ai.service;

import org.opencv.core.Mat;
import java.util.List;
import com.example.camerasurveillancesystem.ai.detector.DetectionResult;

public interface OpenAIService {
    
    /**
     * Phân tích image với GPT-4 Vision
     */
    String analyzeImage(Mat image, String prompt);
    
    /**
     * Generate mô tả scene từ detections
     */
    String generateSceneDescription(List<DetectionResult> detections, Mat image);
    
    /**
     * Phát hiện hành vi bất thường
     */
    boolean detectAnomalousActivity(Mat image, String context);
    
    /**
     * Xác minh detections của YOLOv8
     */
    boolean verifyDetections(List<DetectionResult> detections, Mat image);
}
```

### 2. Implementation với Spring AI

```java
// src/main/java/com/example/camerasurveillancesystem/ai/service/impl/OpenAIServiceImpl.java

package com.example.camerasurveillancesystem.ai.service.impl;

import com.example.camerasurveillancesystem.ai.detector.DetectionResult;
import com.example.camerasurveillancesystem.ai.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAiChatClient chatClient;

    @Override
    public String analyzeImage(Mat image, String prompt) {
        try {
            // Convert Mat to base64
            String base64Image = matToBase64(image);
            
            // Call GPT-4 Vision
            String response = chatClient.call(
                "Analyze this surveillance camera image: " + prompt +
                "\\nImage: data:image/jpeg;base64," + base64Image
            );
            
            log.info("GPT-4 Vision analysis: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error analyzing image with OpenAI", e);
            return null;
        }
    }

    @Override
    public String generateSceneDescription(List<DetectionResult> detections, Mat image) {
        // Tạo context từ YOLOv8 detections
        String detectionsContext = detections.stream()
            .map(d -> String.format("%s (%.2f confidence)", 
                    d.getLabel(), d.getConfidence()))
            .collect(Collectors.joining(", "));
        
        String prompt = String.format(
            "I detected these objects: %s. " +
            "Please provide a natural language description of what's happening in this scene.",
            detectionsContext
        );
        
        return analyzeImage(image, prompt);
    }

    @Override
    public boolean detectAnomalousActivity(Mat image, String context) {
        String prompt = 
            "Analyze this surveillance footage for any suspicious or anomalous activities. " +
            "Context: " + context + ". " +
            "Reply with YES if you detect anything suspicious, NO otherwise.";
        
        String response = analyzeImage(image, prompt);
        return response != null && response.toUpperCase().contains("YES");
    }

    @Override
    public boolean verifyDetections(List<DetectionResult> detections, Mat image) {
        String detectionsContext = detections.stream()
            .map(d -> d.getLabel())
            .collect(Collectors.joining(", "));
        
        String prompt = 
            "I detected these objects using YOLOv8: " + detectionsContext + ". " +
            "Please verify if these detections are accurate. " +
            "Reply with CONFIRMED if accurate, or list corrections needed.";
        
        String response = analyzeImage(image, prompt);
        return response != null && response.toUpperCase().contains("CONFIRMED");
    }

    private String matToBase64(Mat image) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, buffer);
        byte[] bytes = buffer.toArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
```

### 3. Tích hợp vào AiDetectionStreamService

```java
// Thêm vào AiDetectionStreamServiceImpl.java

@Autowired(required = false)
private OpenAIService openAIService;

@Value("${ai.openai.enabled:false}")
private boolean openAiEnabled;

@Value("${ai.openai.use-for-description:false}")
private boolean useForDescription;

private void processFrame(Long cameraId, Mat frame, DetectionContext context) {
    try {
        // ... existing YOLOv8 detection code ...
        
        List<DetectionResult> detections = objectDetector.detect(frame);
        
        if (!detections.isEmpty()) {
            // YOLOv8 detection
            log.debug("YOLOv8 detected {} objects", detections.size());
            
            // Optional: OpenAI analysis
            if (openAiEnabled && openAIService != null && useForDescription) {
                String description = openAIService.generateSceneDescription(detections, frame);
                log.info("GPT-4 Scene description: {}", description);
                // Save description to metadata or separate table
            }
            
            saveDetectionEvent(cameraId, detections);
        }
        
    } catch (Exception e) {
        log.error("Error processing frame", e);
    }
}
```

---

## 📊 Use Cases

### 1. Mô tả chi tiết event

**YOLOv8 Output:**
```
- person (0.92)
- car (0.88)
- bicycle (0.75)
```

**GPT-4 Vision Output:**
```
"A person is riding a bicycle on the sidewalk while a car is parked nearby. 
The scene appears to be in a residential area during daytime. 
The person is wearing a dark jacket and appears to be traveling at a moderate speed."
```

### 2. Phát hiện hành vi bất thường

```java
// Check for suspicious activity
boolean isSuspicious = openAIService.detectAnomalousActivity(
    frame, 
    "Bank ATM surveillance at night"
);

if (isSuspicious) {
    // Trigger alert
    alertService.sendAlert(cameraId, "Suspicious activity detected by AI");
}
```

### 3. Xác minh false positives

```java
// YOLOv8 might detect false positives
if (detections.size() > 10) {
    // Too many detections, verify with GPT-4
    boolean confirmed = openAIService.verifyDetections(detections, frame);
    if (!confirmed) {
        log.warn("GPT-4 rejected YOLOv8 detections as inaccurate");
        return; // Skip saving
    }
}
```

---

## 💰 Cost Estimation

### GPT-4 Vision Pricing (as of 2024)

- **Input**: $0.01 per 1K tokens (~1 image)
- **Output**: $0.03 per 1K tokens

### Cost per camera/day

Giả sử:
- Process 1 frame every 30 seconds = 2,880 frames/day
- Average cost: $0.01 per frame
- **Total: ~$28.80/camera/day** ❌ RẤT ĐẮT!

### Tối ưu chi phí:

1. **Only analyze important events:**
   ```properties
   ai.openai.process-every-n-frames=300  # Every 5 minutes
   ai.openai.min-confidence-trigger=0.9  # High confidence only
   ```
   → Cost: ~$0.50/camera/day ✅

2. **Use for alerts only:**
   ```properties
   ai.openai.use-for-description=false
   ai.openai.use-for-anomaly-detection=true
   ```

3. **Alternative: Use GPT-3.5 for text analysis only**
   - Analyze YOLOv8 detection results (no image)
   - Cost: ~$0.001/request
   - Still useful for generating descriptions

---

## 🎯 Khuyến nghị

### Khi NÊN dùng OpenAI:

✅ Banks, airports (security critical)
✅ Cần mô tả chi tiết cho reports
✅ Phát hiện hành vi phức tạp (loitering, fighting)
✅ Research projects với budget

### Khi KHÔNG NÊN dùng OpenAI:

❌ Detection người/xe cơ bản → **YOLOv8 đủ rồi!**
❌ Budget hạn chế
❌ Real-time processing (GPT-4 Vision slow ~2-5s/image)
❌ Privacy concerns (data sent to OpenAI)

---

## 🚀 Alternative: Local LLMs

Nếu muốn AI analysis nhưng không muốn dùng OpenAI:

### Option 1: LLaVA (Local Vision LLM)

```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Pull LLaVA model
ollama pull llava

# Use in code
String response = ollamaClient.generate("llava", 
    "Describe this surveillance image", 
    base64Image);
```

**Pros:**
- ✅ FREE
- ✅ Privacy (local)
- ✅ No API limits

**Cons:**
- ❌ Need GPU (8GB+ VRAM)
- ❌ Slower than GPT-4
- ❌ Less accurate

### Option 2: MiniGPT-4

Local deployment của GPT-4 style model với vision.

### Option 3: CLIP + GPT-3.5

- Use CLIP (OpenAI) for image understanding → Local/free
- Use GPT-3.5 for text analysis → Cheap ($0.001/request)

---

## 📝 Ví dụ hoàn chỉnh

```java
// Configuration
@Configuration
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
public class OpenAIConfig {
    
    @Bean
    public OpenAiChatClient openAiChatClient(
        @Value("${ai.openai.api-key}") String apiKey
    ) {
        return new OpenAiChatClient(apiKey);
    }
}

// Service usage
@Service
public class SmartAlertService {
    
    @Autowired(required = false)
    private OpenAIService openAIService;
    
    public void processDetectionEvent(Long cameraId, 
                                     List<DetectionResult> detections, 
                                     Mat frame) {
        // Basic YOLOv8 alert
        if (detections.size() > 5) {
            alertService.sendAlert("Multiple objects detected");
        }
        
        // Smart OpenAI analysis (if enabled)
        if (openAIService != null && isHighPriorityCamera(cameraId)) {
            String description = openAIService.generateSceneDescription(
                detections, frame
            );
            
            boolean suspicious = openAIService.detectAnomalousActivity(
                frame, getCameraContext(cameraId)
            );
            
            if (suspicious) {
                alertService.sendPriorityAlert(
                    "SUSPICIOUS ACTIVITY: " + description
                );
            }
        }
    }
}
```

---

## 🔐 Security & Privacy

⚠️ **Lưu ý quan trọng:**

1. **Data Privacy:**
   - Images được gửi đến OpenAI servers
   - Không dùng cho camera có thông tin nhạy cảm
   - Tuân thủ GDPR/regulations

2. **API Key Security:**
   - Không commit API key vào git
   - Dùng environment variables
   - Rotate keys định kỳ

3. **Rate Limiting:**
   - OpenAI có rate limits (500 requests/min)
   - Implement queue và retry logic

---

## 📚 Tài liệu tham khảo

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Spring AI](https://spring.io/projects/spring-ai)
- [GPT-4 Vision Guide](https://platform.openai.com/docs/guides/vision)
- [Ollama (Local LLMs)](https://ollama.ai/)

---

## ✅ Kết luận

OpenAI integration là **OPTIONAL** và **KHÔNG CẦN THIẾT** cho việc detect người và xe cộ cơ bản.

**Khuyến nghị:**
- ✅ Dùng YOLOv8 cho detection (đã có sẵn, miễn phí, real-time)
- ✅ Chỉ thêm OpenAI nếu cần analysis chi tiết và có budget
- ✅ Consider local LLMs nếu cần privacy

**YOLOv8 alone đã rất powerful cho camera surveillance!** 🎯
