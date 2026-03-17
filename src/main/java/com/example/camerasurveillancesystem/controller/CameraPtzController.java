package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.request.PtzMoveRequest;
import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import com.example.camerasurveillancesystem.dto.response.PtzCommandResponse;
import com.example.camerasurveillancesystem.service.CameraPtzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ptz")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Camera PTZ", description = "APIs điều khiển PTZ camera qua ONVIF")
public class CameraPtzController {

    private final CameraPtzService cameraPtzService;

    @PostMapping("/pan-left/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Pan left")
    public ResponseEntity<ApiResponse<PtzCommandResponse>> panLeft(
            @PathVariable Long cameraId,
            @RequestBody(required = false) PtzMoveRequest request
    ) {
        PtzCommandResponse response = cameraPtzService.panLeft(
                cameraId,
                request == null ? null : request.getSpeedPercent(),
                request == null ? null : request.getDurationMs()
        );
        return ResponseEntity.ok(ApiResponse.success("PTZ pan-left thành công", response));
    }

    @PostMapping("/pan-right/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Pan right")
    public ResponseEntity<ApiResponse<PtzCommandResponse>> panRight(
            @PathVariable Long cameraId,
            @RequestBody(required = false) PtzMoveRequest request
    ) {
        PtzCommandResponse response = cameraPtzService.panRight(
                cameraId,
                request == null ? null : request.getSpeedPercent(),
                request == null ? null : request.getDurationMs()
        );
        return ResponseEntity.ok(ApiResponse.success("PTZ pan-right thành công", response));
    }

    @PostMapping("/tilt-up/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Tilt up")
    public ResponseEntity<ApiResponse<PtzCommandResponse>> tiltUp(
            @PathVariable Long cameraId,
            @RequestBody(required = false) PtzMoveRequest request
    ) {
        PtzCommandResponse response = cameraPtzService.tiltUp(
                cameraId,
                request == null ? null : request.getSpeedPercent(),
                request == null ? null : request.getDurationMs()
        );
        return ResponseEntity.ok(ApiResponse.success("PTZ tilt-up thành công", response));
    }

    @PostMapping("/tilt-down/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Tilt down")
    public ResponseEntity<ApiResponse<PtzCommandResponse>> tiltDown(
            @PathVariable Long cameraId,
            @RequestBody(required = false) PtzMoveRequest request
    ) {
        PtzCommandResponse response = cameraPtzService.tiltDown(
                cameraId,
                request == null ? null : request.getSpeedPercent(),
                request == null ? null : request.getDurationMs()
        );
        return ResponseEntity.ok(ApiResponse.success("PTZ tilt-down thành công", response));
    }

    @PostMapping("/stop/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Stop PTZ")
    public ResponseEntity<ApiResponse<PtzCommandResponse>> stop(@PathVariable Long cameraId) {
        log.debug("PTZ stop cameraId={}", cameraId);
        PtzCommandResponse response = cameraPtzService.stop(cameraId);
        return ResponseEntity.ok(ApiResponse.success("PTZ stop thành công", response));
    }
}
