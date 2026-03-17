package com.example.camerasurveillancesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Main application configuration class
 * Configure your Spring Beans here
 */
@Configuration
public class ApplicationConfig {
    
    /**
     * RestTemplate bean for external API calls (e.g., OpenAI)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
