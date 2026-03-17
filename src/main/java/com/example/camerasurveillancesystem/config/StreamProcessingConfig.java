package com.example.camerasurveillancesystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration cho stream processing
 */
@Configuration
@EnableScheduling
public class StreamProcessingConfig {
    // Scheduler đã được enable để StreamMonitorService có thể chạy scheduled tasks
}
