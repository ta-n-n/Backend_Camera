package com.example.camerasurveillancesystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose uploaded media so FE can open snapshot URL directly.
        registry.addResourceHandler("/api/v1/files/**")
                .addResourceLocations("file:uploads/");
    }
}
