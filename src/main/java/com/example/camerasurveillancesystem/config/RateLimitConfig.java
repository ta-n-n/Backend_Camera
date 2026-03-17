package com.example.camerasurveillancesystem.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cấu hình Rate Limiting với Bucket4j
 * Giới hạn số lượng request để tránh abuse và DDoS
 */
@Configuration
public class RateLimitConfig {

    /**
     * Cache lưu Bucket cho mỗi IP/User
     */
    @Bean
    public Map<String, Bucket> rateLimitCache() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Tạo Bucket mặc định: 100 requests/phút
     */
    public Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Bucket cho Auth endpoints: 10 requests/phút (tránh brute force)
     */
    public Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Bucket cho API endpoints thông thường: 200 requests/phút
     */
    public Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Bucket cho PTZ endpoints: 1200 requests/phút
     * PTZ UI thường gửi command liên tục khi người dùng giữ nút điều khiển.
     */
    public Bucket createPtzBucket() {
        Bandwidth limit = Bandwidth.classic(1200, Refill.intervally(1200, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Bucket cho Admin endpoints: 500 requests/phút
     */
    public Bucket createAdminBucket() {
        Bandwidth limit = Bandwidth.classic(500, Refill.intervally(500, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
