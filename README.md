# Camera Surveillance System with AI Detection

Hệ thống giám sát camera thông minh với AI Object Detection (YOLOv8)

## ✨ Tính năng chính

- 🎥 **Kết nối camera RTSP thật** - Hỗ trợ đầy đủ các camera IP qua giao thức RTSP
- 🤖 **AI Object Detection** - Phát hiện người và xe cộ bằng YOLOv8
- 📊 **Real-time monitoring** - Giám sát và phân tích video realtime
- 🔔 **Alert System** - Cảnh báo tự động khi phát hiện sự kiện
- 📈 **Dashboard & Reports** - Thống kê và báo cáo chi tiết
- 🔐 **Security** - JWT authentication & authorization
- 🌐 **WebSocket** - Real-time notifications
- 🗄️ **Database** - Lưu trữ events và metadata

## 🔍 AI Detection

Hệ thống phát hiện các đối tượng:
- 👤 **Người** (person)
- 🚗 **Xe ô tô** (car)
- 🏍️ **Xe máy** (motorcycle)
- 🚌 **Xe buýt** (bus)
- 🚚 **Xe tải** (truck)
- 🚂 **Tàu hỏa** (train)
- 🚲 **Xe đạp** (bicycle)

## 📚 Tài liệu

### Backend
- [HUONG-DAN-CHAY.md](HUONG-DAN-CHAY.md) - Hướng dẫn cài đặt và chạy hệ thống
- [HUONG-DAN-KET-NOI-CAMERA-RTSP.md](HUONG-DAN-KET-NOI-CAMERA-RTSP.md) - Chi tiết kết nối camera RTSP
- [REST-API-EXAMPLES.md](REST-API-EXAMPLES.md) - REST API quick reference
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API documentation đầy đủ
- [STREAM-PROCESSING-IMPLEMENTATION.md](STREAM-PROCESSING-IMPLEMENTATION.md) - Stream processing details
- [OPENAI-INTEGRATION.md](OPENAI-INTEGRATION.md) - (Optional) Tích hợp OpenAI

### Frontend
- [FRONTEND-CONNECTION-SUMMARY.md](FRONTEND-CONNECTION-SUMMARY.md) - 🎯 **ĐỌC FILE NÀY TRƯỚC!** Tóm tắt kết nối FE
- [FRONTEND-INTEGRATION-GUIDE.md](FRONTEND-INTEGRATION-GUIDE.md) - Hướng dẫn chi tiết tích hợp Frontend

### Tổng hợp
- [THAY-DOI-VA-CAI-TIEN.md](THAY-DOI-VA-CAI-TIEN.md) - Tổng hợp tất cả thay đổi đã thực hiện

## 🚀 Quick Start

### 1. Chuẩn bị

```bash
# Java 17+
java -version

# Maven 3.6+
mvn -version

# MySQL/PostgreSQL running
```

### 2. Cấu hình

Tạo file `.env` hoặc set environment variables:

```bash
DB_URL=jdbc:mysql://localhost:3306/camera_db
DB_USERNAME=root
DB_PASSWORD=your_password
```

### 3. Chạy ứng dụng

```bash
# Build
mvn clean install

# Run (KHÔNG dùng profile 'seed' để tránh tạo camera ảo)
mvn spring-boot:run
```

### 4. Thêm camera RTSP thật

Xem chi tiết trong [HUONG-DAN-KET-NOI-CAMERA-RTSP.md](HUONG-DAN-KET-NOI-CAMERA-RTSP.md)

```bash
# Ví dụ: Thêm camera qua API
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Camera cổng chính",
    "code": "CAM-001",
    "rtspUrl": "rtsp://admin:pass@192.168.1.100:554/stream1",
    "status": "ACTIVE"
  }'
```

### 5. Bật AI Detection

```bash
# Bật detection cho camera ID 1
curl -X POST http://localhost:8081/api/stream-processing/camera/1/start-detection
```

## 🏗️ Project Structure

```
src/
├─ main/
│  ├─ java/com/example/camerasurveillancesystem/
│  │   ├─ config/          # Spring Bean configurations
│  │   ├─ constant/        # Constants & Enums
│  │   │     └─ enums/     # Enum definitions
│  │   ├─ controller/      # REST API endpoints
│  │   ├─ domain/          # JPA entities
│  │   ├─ dto/             # Data Transfer Objects
│  │   │     ├─ mapper/    # MapStruct mappers
│  │   │     ├─ request/   # Request DTOs
│  │   │     └─ response/  # Response DTOs
│  │   ├─ exception/       # Global exception handlers
│  │   ├─ repository/      # JPA repositories
│  │   ├─ security/        # Security filters & utils
│  │   ├─ service/         # Business logic services
│  │   │     └─ impl/      # Service implementations
│  │   ├─ specification/   # JPA Specifications for dynamic filtering
│  │   ├─ utils/           # Utility classes
│  │   ├─ validation/      # Custom validators
│  │   ├─ websocket/       # WebSocket handlers & messaging
│  │   └─ CameraSurveillanceSystemApplication.java
│  └─ resources/
│      ├─ db/changelog/    # Liquibase database migrations
│      ├─ static/          # Static files
│      ├─ templates/       # Thymeleaf templates
│      ├─ application.properties
│      └─ messages.properties
└─ test/
   ├─ java/com/example/camerasurveillancesystem/
   │  ├─ controller/       # Controller tests
   │  ├─ service/          # Service tests
   │  └─ CameraSurveillanceSystemApplicationTests.java
   └─ resources/           # Test resources
```

## Package Descriptions

- **config**: Spring configuration classes (ApplicationConfig, SecurityConfig, WebSocketConfig, etc.)
- **constant**: Application constants and enums
- **controller**: REST API controllers
- **domain**: JPA entity classes
- **dto**: Data Transfer Objects for request/response handling
- **exception**: Custom exceptions and global exception handler
- **repository**: JPA repositories for database access
- **security**: Security configurations and JWT handling
- **service**: Business logic layer
- **specification**: Dynamic query specifications
- **utils**: Helper and utility classes
- **validation**: Custom validation logic
- **websocket**: WebSocket communication handlers

## Getting Started

1. Configure your database in `application.properties`
2. Run the application using `mvn spring-boot:run`
3. Access the API at `http://localhost:8080`

## Technologies

- Spring Boot
- Spring Data JPA
- Spring Security
- WebSocket
- Liquibase
- MapStruct
