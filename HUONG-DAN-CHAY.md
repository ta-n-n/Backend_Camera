# 🚀 HƯỚNG DẪN CHẠY HỆ THỐNG

## 📋 Tổng quan

Hệ thống Camera Surveillance đã được tích hợp đầy đủ:
- ✅ RTSP Stream Processing
- ✅ AI Object Detection (YOLOv8)
- ✅ Auto Monitoring & Restart
- ✅ REST APIs đầy đủ

---

## 🛠️ Yêu cầu

### 1. Phần mềm cần cài

- Java 17+
- Maven 3.6+
- Docker (cho RTSP server)
- FFmpeg (để stream video)
- PostgreSQL (hoặc MySQL)

### 2. Dependencies

Đã có sẵn trong `pom.xml`:
- Spring Boot
- OpenCV 4.9.0
- ONNX Runtime (cho YOLOv8)
- Liquibase

---

## ⚡ Quick Start

### Bước 1: Clone và Build

```bash
# Clone project (nếu chưa có)
cd "Camera _Surveillance _System"

# Build project
mvn clean install -DskipTests
```

### Bước 2: Cấu hình Database

Sửa file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/camera_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# Liquibase sẽ tự động tạo tables
spring.liquibase.enabled=true
```

### Bước 3: Tải AI Model

```bash
# Model YOLOv8n đã có sẵn trong thư mục models/
ls models/yolov8n.onnx

# Nếu chưa có, download tại:
# https://github.com/ultralytics/assets/releases/download/v0.0.0/yolov8n.onnx
```

### Bước 4: Chạy Backend

```bash
# QUAN TRỌNG: Không dùng profile 'seed' để tránh tạo 50 camera ảo
mvn spring-boot:run
```

Backend sẽ chạy tại: http://localhost:8081

Swagger UI: http://localhost:8081/swagger-ui.html

---

## 📹 Kết nối Camera RTSP Thật

Hệ thống được thiết kế để kết nối trực tiếp với camera IP qua RTSP.

### ✅ Khuyến nghị: Dùng Camera Thật

Xem chi tiết trong [HUONG-DAN-KET-NOI-CAMERA-RTSP.md](HUONG-DAN-KET-NOI-CAMERA-RTSP.md)

**Các loại camera hỗ trợ:**
- Hikvision
- Dahua
- TP-Link
- Axis
- Uniview
- Bất kỳ camera IP nào hỗ trợ RTSP

**Ví dụ RTSP URL:**
```
rtsp://admin:password@192.168.1.100:554/Streaming/Channels/101
```

**Thêm camera qua API:**
```bash
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Camera cổng chính",
    "code": "CAM-GATE-001",
    "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/stream1",
    "status": "ACTIVE",
    "resolution": "1920x1080"
  }'
```

---

## 🧪 (Optional) Test với Video Files

Nếu chưa có camera thật, bạn có thể test với video files:

### Bước 1: Chuẩn bị video

Tạo thư mục lưu video (ngoài project):

```bash
# Windows
mkdir D:\camera-video
# Copy video files vào đây

# Linux
mkdir ~/camera-video
# Copy video files vào đây
```

Ví dụ:
```
D:\camera-video\
   traffic.mp4
   parking.mp4
   street.mp4
```

### Bước 2: Chạy RTSP Server (MediaMTX)

```bash
docker run -d \
  --name mediamtx \
  -p 8554:8554 \
  --restart unless-stopped \
  bluenviron/mediamtx
```

Kiểm tra RTSP server:
```bash
docker logs mediamtx
```

### Bước 3: Stream video bằng FFmpeg

Mở terminal mới và chạy:

#### Windows:
```cmd
ffmpeg -re -stream_loop -1 -i D:\camera-video\traffic.mp4 ^
-c copy -f rtsp rtsp://localhost:8554/camera1
```

#### Linux/Mac:
```bash
ffmpeg -re -stream_loop -1 -i ~/camera-video/traffic.mp4 \
-c copy -f rtsp rtsp://localhost:8554/camera1
```

**Giải thích tham số:**
- `-re`: Real-time mode (stream với tốc độ thực)
- `-stream_loop -1`: Loop vô hạn
- `-c copy`: Copy codec (không re-encode)
- `-f rtsp`: Output format RTSP

**Để stream nhiều videos, mở nhiều terminals:**
```bash
# Terminal 1
ffmpeg -re -stream_loop -1 -i ./video1.mp4 -c copy -f rtsp rtsp://localhost:8554/camera1

# Terminal 2
ffmpeg -re -stream_loop -1 -i ./video2.mp4 -c copy -f rtsp rtsp://localhost:8554/camera2

# Terminal 3
ffmpeg -re -stream_loop -1 -i ./video3.mp4 -c copy -f rtsp rtsp://localhost:8554/camera3
```

---

## 🔧 Test APIs

### 1. Login để lấy JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{
  "username": "admin",
  "password": "admin123"
}'
```

Lưu lại token trong response.

### 2. Tạo Camera

```bash
curl -X POST http://localhost:8080/api/v1/cameras \
-H "Authorization: Bearer YOUR_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "name": "Camera Parking Lot",
  "code": "CAM001",
  "rtspUrl": "rtsp://localhost:8554/camera1",
  "status": "ACTIVE",
  "resolution": "1920x1080",
  "frameRate": 30
}'
```

### 3. Start Stream Monitoring

```bash
curl -X POST http://localhost:8080/api/v1/stream/monitor/start \
-H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Start AI Detection

```bash
# Start cho camera ID 1
curl -X POST http://localhost:8080/api/v1/stream/detection/start/1 \
-H "Authorization: Bearer YOUR_TOKEN"

# Hoặc start tất cả cameras
curl -X POST http://localhost:8080/api/v1/stream/detection/start-all \
-H "Authorization: Bearer YOUR_TOKEN"
```

### 5. Kiểm tra Status

```bash
# Status của camera 1
curl -X GET http://localhost:8080/api/v1/stream/status/1 \
-H "Authorization: Bearer YOUR_TOKEN"

# Status tất cả cameras
curl -X GET http://localhost:8080/api/v1/stream/status/all \
-H "Authorization: Bearer YOUR_TOKEN"
```

### 6. Xem Detections

```bash
# Latest detections
curl -X GET http://localhost:8080/api/v1/stream/detection/latest/1 \
-H "Authorization: Bearer YOUR_TOKEN"

# Historical AI events
curl -X GET "http://localhost:8080/api/v1/ai-events?cameraId=1&page=0&size=20" \
-H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 Test Flow Hoàn chỉnh

### Postman Collection

1. Import collection vào Postman
2. Chạy theo thứ tự:

```
1. Auth / Login
   → Lấy token, set vào Environment variable

2. Camera / Create Camera
   → Tạo camera với rtspUrl

3. Stream / Start Monitoring
   → Bật monitoring service

4. Stream / Start Detection (Single)
   → Bật AI detection cho camera 1

5. Stream / Get Status
   → Kiểm tra stream đang chạy

6. Stream / Get Latest Detections
   → Xem objects được phát hiện

7. AI Events / Search Events
   → Xem lịch sử detections
```

---

## 📊 Monitor System

### Logs

```bash
# Xem logs realtime
tail -f logs/application.log

# Hoặc nếu chạy bằng mvn
# Logs sẽ hiển thị trực tiếp trong terminal
```

**Logs cần chú ý:**
- `Starting stream for camera X` - Stream bắt đầu
- `Detected N objects in camera X` - Phát hiện objects
- `Stream not running for camera X` - Stream bị disconnect
- `Auto-restarting stream for camera X` - Tự động restart

### Database

Kiểm tra database:

```sql
-- Cameras
SELECT * FROM cameras ORDER BY id;

-- Stream status
SELECT id, camera_id, stream_url, is_active FROM camera_streams;

-- AI Events
SELECT 
  id, 
  camera_id, 
  event_type, 
  object_count, 
  confidence, 
  detected_at 
FROM ai_events 
ORDER BY detected_at DESC 
LIMIT 20;

-- Detection objects
SELECT 
  aeo.id, 
  ae.camera_id, 
  aeo.class_name, 
  aeo.confidence,
  ae.detected_at
FROM ai_event_objects aeo
JOIN ai_events ae ON aeo.event_id = ae.id
ORDER BY ae.detected_at DESC
LIMIT 50;

-- Health logs
SELECT * FROM camera_health_logs ORDER BY checked_at DESC LIMIT 20;
```

---

## ⚙️ Cấu hình nâng cao

### Điều chỉnh Detection Config

```bash
curl -X POST http://localhost:8080/api/v1/stream/detection/config \
-H "Authorization: Bearer YOUR_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "cameraId": 1,
  "confidenceThreshold": 0.7,
  "frameSkip": 10,
  "autoRestart": true
}'
```

**Parameters:**
- `confidenceThreshold`: 0.5 - 0.9 (càng cao càng chính xác nhưng detect ít hơn)
- `frameSkip`: 5 - 20 (detect mỗi N frames, tăng để giảm CPU)
- `autoRestart`: true/false (tự động restart khi disconnect)

### Performance Tuning

#### Giảm CPU Usage:
```json
{
  "confidenceThreshold": 0.6,
  "frameSkip": 15
}
```

#### Tăng Accuracy:
```json
{
  "confidenceThreshold": 0.8,
  "frameSkip": 3
}
```

---

## 🐛 Troubleshooting

### Lỗi: "OpenCV not loaded"

```bash
# Kiểm tra OpenCV native library
java -Djava.library.path=/usr/local/lib -jar target/*.jar
```

### Lỗi: "Failed to open stream"

1. Kiểm tra RTSP server:
```bash
docker ps | grep mediamtx
docker logs mediamtx
```

2. Kiểm tra FFmpeg đang stream:
```bash
# Phải thấy ffmpeg process đang chạy
ps aux | grep ffmpeg
```

3. Test RTSP URL:
```bash
ffplay rtsp://localhost:8554/camera1
```

### Lỗi: "Model not found"

```bash
# Kiểm tra file model
ls -lh models/yolov8n.onnx

# Download lại nếu thiếu
curl -L -o models/yolov8n.onnx \
https://github.com/ultralytics/assets/releases/download/v0.0.0/yolov8n.onnx
```

### Stream bị lag

1. Tăng `frameSkip`:
```json
{"cameraId": 1, "frameSkip": 20}
```

2. Giảm resolution của video stream
3. Giảm số cameras chạy đồng thời

---

## 📚 Tài liệu chi tiết

- API Documentation: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- AI Module Structure: [AI-MODULE-STRUCTURE.md](AI-MODULE-STRUCTURE.md)
- Model Download Guide: [HUONG-DAN-TAI-MODEL.md](HUONG-DAN-TAI-MODEL.md)

---

## ✅ Checklist

Trước khi chạy, đảm bảo:

- [ ] Java 17+ installed
- [ ] Database running và đã config trong application.properties
- [ ] AI model (yolov8n.onnx) có trong thư mục models/
- [ ] RTSP server (MediaMTX) đang chạy
- [ ] FFmpeg đã cài đặt
- [ ] Video files đã chuẩn bị
- [ ] FFmpeg đang stream video đến RTSP server
- [ ] Backend đã start thành công
- [ ] Đã login và có JWT token

---

## 🎉 Next Steps

Sau khi hệ thống chạy thành công:

1. Tích hợp WebSocket để push detections realtime đến Frontend
2. Thêm video recording functionality
3. Implement face recognition
4. Add email/SMS alerts cho critical events
5. Dashboard analytics và statistics

Happy coding! 🚀
