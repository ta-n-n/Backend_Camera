package com.example.camerasurveillancesystem.filter;

import com.example.camerasurveillancesystem.config.RateLimitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Filter áp dụng rate limiting cho các requests
 * Giới hạn số lượng request dựa trên IP address
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );

    private final Map<String, Bucket> rateLimitCache;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = getClientId(request);
        String requestUri = request.getRequestURI();
        String endpointGroup = resolveEndpointGroup(requestUri);
        String bucketKey = clientId + ":" + endpointGroup;

        // Lấy hoặc tạo bucket cho client
        Bucket bucket = rateLimitCache.computeIfAbsent(bucketKey, key -> selectBucket(endpointGroup));

        // Try to consume 1 token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request được phép - thêm rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            if (waitForRefill <= 0) {
                waitForRefill = 1;
            }
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            applyCorsHeaders(request, response);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Quá nhiều requests. Vui lòng thử lại sau " + waitForRefill + " giây.");
            errorResponse.put("errorCode", "RATE_LIMIT_EXCEEDED");
            errorResponse.put("retryAfter", waitForRefill);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            
            log.warn("Rate limit exceeded for client: {} on URI: {} (group: {})", clientId, requestUri, endpointGroup);
        }
    }

    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    /**
     * Lấy client identifier (IP address + User-Agent)
     */
    private String getClientId(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return ip + "_" + (userAgent != null ? userAgent.hashCode() : "unknown");
    }

    /**
     * Lấy IP address thực của client (xử lý proxy/load balancer)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Get first IP if multiple (proxy chain)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Chọn bucket phù hợp dựa trên endpoint
     */
    private Bucket selectBucket(String endpointGroup) {
        if ("AUTH".equals(endpointGroup)) {
            return rateLimitConfig.createAuthBucket();
        } else if ("ADMIN".equals(endpointGroup)) {
            return rateLimitConfig.createAdminBucket();
        } else if ("PTZ".equals(endpointGroup)) {
            return rateLimitConfig.createPtzBucket();
        } else if ("API".equals(endpointGroup)) {
            return rateLimitConfig.createApiBucket();
        } else {
            return rateLimitConfig.createDefaultBucket();
        }
    }

    private String resolveEndpointGroup(String requestUri) {
        if (requestUri.startsWith("/api/v1/auth/")) {
            return "AUTH";
        }
        if (requestUri.startsWith("/api/v1/admin/")) {
            return "ADMIN";
        }
        if (requestUri.startsWith("/api/v1/ptz/")) {
            return "PTZ";
        }
        if (requestUri.startsWith("/api/v1/")) {
            return "API";
        }
        return "DEFAULT";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Không apply rate limit cho health check, swagger, static resources
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) ||
               path.startsWith("/api/v1/system/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }
}
