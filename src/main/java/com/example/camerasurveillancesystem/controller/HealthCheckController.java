package com.example.camerasurveillancesystem.controller;

import com.example.camerasurveillancesystem.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/health")
@RequiredArgsConstructor
public class HealthCheckController {

    /**
     * Get system health status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    /**
     * Get system metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Memory metrics
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", memoryMXBean.getHeapMemoryUsage().getUsed());
        memory.put("heapMax", memoryMXBean.getHeapMemoryUsage().getMax());
        memory.put("heapCommitted", memoryMXBean.getHeapMemoryUsage().getCommitted());
        memory.put("nonHeapUsed", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        metrics.put("memory", memory);

        // CPU metrics
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("availableProcessors", osMXBean.getAvailableProcessors());
        cpu.put("systemLoadAverage", osMXBean.getSystemLoadAverage());
        metrics.put("cpu", cpu);

        // Thread metrics
        Map<String, Object> threads = new HashMap<>();
        threads.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        threads.put("peakThreadCount", ManagementFactory.getThreadMXBean().getPeakThreadCount());
        metrics.put("threads", threads);

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
