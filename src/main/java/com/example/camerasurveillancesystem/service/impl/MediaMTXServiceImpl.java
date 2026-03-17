package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.service.MediaMTXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MediaMTXServiceImpl implements MediaMTXService {

    private final RestTemplate restTemplate;

    @Value("${mediamtx.enabled:false}")
    private boolean enabled;

    @Value("${mediamtx.api.url:http://localhost:9997}")
    private String apiUrl;

    @Value("${mediamtx.hls.url:http://localhost:8888}")
    private String hlsBaseUrl;

    @Value("${mediamtx.webrtc.url:http://localhost:8889}")
    private String webrtcBaseUrl;

    @Value("${mediamtx.rtsp.url:rtsp://localhost:8554}")
    private String rtspBaseUrl;

    @Value("${mediamtx.source-on-demand:false}")
    private boolean sourceOnDemand;

    public MediaMTXServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Map<String, Object> buildPathConfig(String rtspUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("source", rtspUrl);
        // Low-latency profile: keep source warm when sourceOnDemand=false.
        body.put("sourceOnDemand", sourceOnDemand);
        body.put("rtspTransport", "tcp");
        return body;
    }

    @Override
    public boolean registerCamera(String cameraCode, String rtspUrl) {
        if (!enabled || rtspUrl == null || rtspUrl.isBlank()) {
            log.debug("MediaMTX disabled or no RTSP URL, skipping register for {}", cameraCode);
            return false;
        }

        try {
            String url = apiUrl + "/v3/config/paths/add/" + cameraCode;
            Map<String, Object> body = buildPathConfig(rtspUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Registered camera {} in MediaMTX with source {}", cameraCode, rtspUrl);
                return true;
            }
        } catch (HttpClientErrorException.Conflict e) {
            // Path đã tồn tại → update thay vì báo lỗi
            log.warn("Camera {} already exists in MediaMTX, updating instead", cameraCode);
            return updateCamera(cameraCode, rtspUrl);
        } catch (HttpClientErrorException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && responseBody.toLowerCase().contains("path already exists")) {
                log.warn("Camera {} already exists in MediaMTX (400), updating instead", cameraCode);
                return updateCamera(cameraCode, rtspUrl);
            }
            log.error("Failed to register camera {} in MediaMTX (bad request): {}", cameraCode, responseBody);
        } catch (Exception e) {
            log.error("Failed to register camera {} in MediaMTX: {}", cameraCode, e.getMessage());
        }
        return false;
    }

    @Override
    public void unregisterCamera(String cameraCode) {
        if (!enabled) {
            return;
        }

        try {
            String url = apiUrl + "/v3/config/paths/delete/" + cameraCode;
            restTemplate.delete(url);
            log.info("Unregistered camera {} from MediaMTX", cameraCode);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Camera {} not found in MediaMTX, nothing to unregister", cameraCode);
        } catch (Exception e) {
            log.error("Failed to unregister camera {} from MediaMTX: {}", cameraCode, e.getMessage());
        }
    }

    @Override
    public boolean updateCamera(String cameraCode, String newRtspUrl) {
        if (!enabled || newRtspUrl == null || newRtspUrl.isBlank()) {
            return false;
        }

        try {
            String url = apiUrl + "/v3/config/paths/patch/" + cameraCode;
            Map<String, Object> body = buildPathConfig(newRtspUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);

            log.info("Updated camera {} in MediaMTX with new source {}", cameraCode, newRtspUrl);
            return true;
        } catch (Exception e) {
            log.warn("PATCH update failed for camera {} in MediaMTX: {}. Fallback to delete+add", cameraCode, e.getMessage());

            try {
                // Fallback cho môi trường RestTemplate không hỗ trợ PATCH
                unregisterCamera(cameraCode);

                String addUrl = apiUrl + "/v3/config/paths/add/" + cameraCode;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> addRequest = new HttpEntity<>(buildPathConfig(newRtspUrl), headers);
                ResponseEntity<String> addResponse = restTemplate.postForEntity(addUrl, addRequest, String.class);

                if (addResponse.getStatusCode().is2xxSuccessful()) {
                    log.info("Recreated camera {} path in MediaMTX with new source {}", cameraCode, newRtspUrl);
                    return true;
                }
            } catch (Exception recreateException) {
                log.error("Failed to recreate camera {} in MediaMTX: {}", cameraCode, recreateException.getMessage());
            }

            return false;
        }
    }

    @Override
    public String getHlsUrl(String cameraCode) {
        return hlsBaseUrl + "/" + cameraCode + "/index.m3u8";
    }

    @Override
    public String getWebRtcUrl(String cameraCode) {
        return webrtcBaseUrl + "/" + cameraCode;
    }

    @Override
    public String getRtspUrl(String cameraCode) {
        return rtspBaseUrl + "/" + cameraCode;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
