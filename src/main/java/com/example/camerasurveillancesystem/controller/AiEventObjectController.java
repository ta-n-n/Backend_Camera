package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.AiEventObjectResponse;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.service.AiEventObjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-event-objects")
@RequiredArgsConstructor
@Tag(name = "AI Event Objects", description = "Quản lý các đối tượng được phát hiện trong sự kiện AI")
public class AiEventObjectController {

    private final AiEventObjectService objectService;

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin object theo ID")
    public ResponseEntity<ApiResponse<AiEventObjectResponse>> getObjectById(
            @Parameter(description = "ID của object") @PathVariable Long id) {
        AiEventObjectResponse response = objectService.getObjectById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Lấy tất cả objects của một AI event")
    public ResponseEntity<ApiResponse<List<AiEventObjectResponse>>> getObjectsByEventId(
            @Parameter(description = "ID của AI event") @PathVariable Long eventId) {
        List<AiEventObjectResponse> response = objectService.getObjectsByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{objectType}")
    @Operation(summary = "Lấy objects theo loại (PERSON, CAR, TRUCK, etc.)")
    public ResponseEntity<ApiResponse<List<AiEventObjectResponse>>> getObjectsByType(
            @Parameter(description = "Loại object (PERSON, CAR, TRUCK, BIKE, ANIMAL, FACE)") 
            @PathVariable String objectType) {
        List<AiEventObjectResponse> response = objectService.getObjectsByType(objectType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/confidence")
    @Operation(summary = "Lấy objects theo độ tin cậy tối thiểu")
    public ResponseEntity<ApiResponse<List<AiEventObjectResponse>>> getObjectsByMinConfidence(
            @Parameter(description = "Độ tin cậy tối thiểu (0.0 - 1.0)") 
            @RequestParam Double minConfidence) {
        List<AiEventObjectResponse> response = objectService.getObjectsByMinConfidence(minConfidence);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/event/{eventId}/type/{objectType}")
    @Operation(summary = "Lấy objects theo event và loại")
    public ResponseEntity<ApiResponse<List<AiEventObjectResponse>>> getObjectsByEventAndType(
            @Parameter(description = "ID của AI event") @PathVariable Long eventId,
            @Parameter(description = "Loại object") @PathVariable String objectType) {
        List<AiEventObjectResponse> response = objectService.getObjectsByEventAndType(eventId, objectType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/event/{eventId}/count")
    @Operation(summary = "Đếm số lượng objects trong một event")
    public ResponseEntity<ApiResponse<Long>> countObjectsByEvent(
            @Parameter(description = "ID của AI event") @PathVariable Long eventId) {
        long count = objectService.countObjectsByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/types")
    @Operation(summary = "Lấy danh sách tất cả các loại object đã phát hiện")
    public ResponseEntity<ApiResponse<List<String>>> getAllObjectTypes() {
        List<String> types = objectService.getAllObjectTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa object")
    public ResponseEntity<ApiResponse<Void>> deleteObject(
            @Parameter(description = "ID của object") @PathVariable Long id) {
        objectService.deleteObject(id);
        return ResponseEntity.ok(ApiResponse.success("Object deleted successfully", null));
    }
}
