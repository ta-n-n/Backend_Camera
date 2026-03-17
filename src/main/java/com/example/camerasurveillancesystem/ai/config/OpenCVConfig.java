package com.example.camerasurveillancesystem.ai.config;

import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Cấu hình OpenCV
 */
@Configuration
@Slf4j
public class OpenCVConfig {

    @PostConstruct
    public void init() {
        try {
            // Load OpenCV native library
            OpenCV.loadLocally();
            log.info("OpenCV loaded successfully");
            log.info("OpenCV version: {}", org.opencv.core.Core.VERSION);
        } catch (Exception e) {
            log.error("Failed to load OpenCV", e);
            throw new RuntimeException("Failed to initialize OpenCV", e);
        }
    }

    @Bean
    public String openCVVersion() {
        return org.opencv.core.Core.VERSION;
    }
}
