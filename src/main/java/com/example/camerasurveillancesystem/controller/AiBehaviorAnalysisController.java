package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.ai.detector.DetectionResult;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Controller cho AI Behavior Analysis với OpenAI
 * Chỉ enable khi ai.openai.enabled=true
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai-analysis")
@RequiredArgsConstructor
@Tag(name = "AI Behavior Analysis", description = "OpenAI-powered behavior analysis (Optional)")
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true", matchIfMissing = false)
public class AiBehaviorAnalysisController {

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Phân tích hành vi từ detections
     * Frontend gọi endpoint này để nhận AI analysis
     */
    @PostMapping("/analyze-behavior")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Analyze behavior from detection results using OpenAI")
    public ResponseEntity<ApiResponse<BehaviorAnalysisResponse>> analyzeBehavior(
            @RequestBody BehaviorAnalysisRequest request) {
        
        log.info("Analyzing behavior for camera {} with {} detections", 
                request.getCameraId(), request.getDetections().size());

        try {
            // Validate API key
            if (apiKey == null || apiKey.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<BehaviorAnalysisResponse>builder()
                        .success(false)
                        .message("OpenAI API key not configured")
                        .build());
            }

            // Build prompt from detections
            String prompt = buildPrompt(request);
            
            // Call OpenAI API
            String analysis = callOpenAI(prompt);

            BehaviorAnalysisResponse response = new BehaviorAnalysisResponse();
            response.setCameraId(request.getCameraId());
            response.setAnalysis(analysis);
            response.setContext(request.getContext());
            response.setDetectionCount(request.getDetections().size());

            return ResponseEntity.ok(ApiResponse.<BehaviorAnalysisResponse>builder()
                .success(true)
                .message("Behavior analysis completed")
                .data(response)
                .build());

        } catch (Exception e) {
            log.error("Error analyzing behavior", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<BehaviorAnalysisResponse>builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Phát hiện hành vi bất thường
     */
    @PostMapping("/detect-anomaly")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Detect anomalous/suspicious behavior")
    public ResponseEntity<ApiResponse<AnomalyDetectionResponse>> detectAnomaly(
            @RequestBody BehaviorAnalysisRequest request) {
        
        try {
            String prompt = buildAnomalyDetectionPrompt(request);
            String analysis = callOpenAI(prompt);

            boolean isSuspicious = analysis.toUpperCase().contains("SUSPICIOUS") ||
                                 analysis.toUpperCase().contains("ANOMALY") ||
                                 analysis.toUpperCase().contains("YES");

            AnomalyDetectionResponse response = new AnomalyDetectionResponse();
            response.setCameraId(request.getCameraId());
            response.setSuspicious(isSuspicious);
            response.setDescription(analysis);
            response.setConfidence(isSuspicious ? 0.8 : 0.2);

            return ResponseEntity.ok(ApiResponse.<AnomalyDetectionResponse>builder()
                .success(true)
                .message("Anomaly detection completed")
                .data(response)
                .build());

        } catch (Exception e) {
            log.error("Error detecting anomaly", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<AnomalyDetectionResponse>builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Generate scene description
     */
    @PostMapping("/describe-scene")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Generate human-readable scene description")
    public ResponseEntity<ApiResponse<SceneDescriptionResponse>> describeScene(
            @RequestBody BehaviorAnalysisRequest request) {
        
        try {
            String prompt = buildSceneDescriptionPrompt(request);
            String description = callOpenAI(prompt);

            SceneDescriptionResponse response = new SceneDescriptionResponse();
            response.setCameraId(request.getCameraId());
            response.setDescription(description);
            response.setDetectionCount(request.getDetections().size());

            return ResponseEntity.ok(ApiResponse.<SceneDescriptionResponse>builder()
                .success(true)
                .message("Scene description generated")
                .data(response)
                .build());

        } catch (Exception e) {
            log.error("Error describing scene", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<SceneDescriptionResponse>builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    // Helper methods

    private String buildPrompt(BehaviorAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this surveillance camera scene:\n\n");
        
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            prompt.append("Context: ").append(request.getContext()).append("\n\n");
        }
        
        prompt.append("Detected objects:\n");
        for (DetectionInfo det : request.getDetections()) {
            prompt.append("- ").append(det.getLabel())
                  .append(" (confidence: ").append(String.format("%.2f", det.getConfidence()))
                  .append(")\n");
        }
        
        prompt.append("\nProvide a brief analysis of what's happening in this scene. ");
        prompt.append("Is there anything noteworthy or requiring attention?");
        
        return prompt.toString();
    }

    private String buildAnomalyDetectionPrompt(BehaviorAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this surveillance footage for suspicious or anomalous activities:\n\n");
        
        if (request.getContext() != null) {
            prompt.append("Context: ").append(request.getContext()).append("\n\n");
        }
        
        prompt.append("Detected objects:\n");
        for (DetectionInfo det : request.getDetections()) {
            prompt.append("- ").append(det.getLabel()).append("\n");
        }
        
        prompt.append("\nIs there any suspicious or unusual behavior? ");
        prompt.append("Start your response with YES or NO, then explain.");
        
        return prompt.toString();
    }

    private String buildSceneDescriptionPrompt(BehaviorAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Describe this surveillance scene in natural language:\n\n");
        
        prompt.append("Detected objects:\n");
        for (DetectionInfo det : request.getDetections()) {
            prompt.append("- ").append(det.getLabel())
                  .append(" (").append(String.format("%.0f%%", det.getConfidence() * 100))
                  .append(" confidence)\n");
        }
        
        prompt.append("\nProvide a concise, natural description of what's happening in 2-3 sentences.");
        
        return prompt.toString();
    }

    private String callOpenAI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", "You are an AI security analyst."),
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", 300,
            "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://api.openai.com/v1/chat/completions",
            request,
            Map.class
        );
        
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Empty response from OpenAI");
        }

        List<Map<String, Object>> choices = (List) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in OpenAI response");
        }

        Map<String, Object> message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    // DTOs

    @Data
    public static class BehaviorAnalysisRequest {
        private Long cameraId;
        private List<DetectionInfo> detections;
        private String context;
    }

    @Data
    public static class DetectionInfo {
        private String label;
        private String objectType;
        private Double confidence;
    }

    @Data
    public static class BehaviorAnalysisResponse {
        private Long cameraId;
        private String analysis;
        private String context;
        private Integer detectionCount;
    }

    @Data
    public static class AnomalyDetectionResponse {
        private Long cameraId;
        private Boolean suspicious;
        private String description;
        private Double confidence;
    }

    @Data
    public static class SceneDescriptionResponse {
        private Long cameraId;
        private String description;
        private Integer detectionCount;
    }
}
