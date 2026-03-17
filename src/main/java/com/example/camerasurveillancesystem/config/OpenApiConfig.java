package com.example.camerasurveillancesystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Camera Surveillance System API",
                version = "1.0.0",
                description = """
                        # Hệ thống giám sát camera thông minh với AI
                        
                        API REST cho hệ thống giám sát camera tích hợp AI phát hiện đối tượng, 
                        cảnh báo realtime, quản lý video recording và nhiều chức năng khác.
                        
                        ## Tính năng chính:
                        - 📹 Quản lý camera và streams
                        - 🤖 AI Object Detection (YOLOv8)
                        - 🚨 Cảnh báo thông minh realtime
                        - 📊 Dashboard và báo cáo
                        - 👤 Quản lý người dùng và phân quyền
                        - 💾 Lưu trữ video và snapshot
                        - 🔔 Thông báo đa kênh (Email, SMS, Webhook)
                        
                        ## Authentication:
                        Sử dụng JWT Bearer token. Đăng ký/đăng nhập tại `/api/v1/auth/login` hoặc `/api/v1/auth/register`
                        """,
                contact = @Contact(
                        name = "Camera Surveillance Team",
                        email = "support@camerasurveillance.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8081"
                ),
                @Server(
                        description = "Production Server",
                        url = "https://api.camerasurveillance.com"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Nhập JWT token. Lấy token từ endpoint /api/v1/auth/login hoặc /api/v1/auth/register"
)
public class OpenApiConfig {
}
