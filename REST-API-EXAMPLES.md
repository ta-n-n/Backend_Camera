# 🔌 REST API Examples - Quick Reference

## 📋 Authentication

Tất cả API (trừ login) đều cần JWT token trong header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

### Login để lấy token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

---

## 📹 Camera Management

### 1. Thêm Camera RTSP

```bash
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera cổng chính",
    "code": "CAM-GATE-001",
    "model": "Hikvision DS-2CD2143G0-I",
    "manufacturer": "Hikvision",
    "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/Streaming/Channels/101",
    "snapshotUrl": "http://192.168.1.100/snapshot.jpg",
    "status": "ACTIVE",
    "resolution": "1920x1080",
    "frameRate": 30,
    "description": "Camera giám sát cổng ra vào chính"
  }'
```

### 2. Lấy danh sách cameras

```bash
curl -X GET "http://localhost:8081/api/cameras?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Lấy thông tin camera theo ID

```bash
curl -X GET http://localhost:8081/api/cameras/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Cập nhật camera

```bash
curl -X PUT http://localhost:8081/api/cameras/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera cổng chính - Updated",
    "rtspUrl": "rtsp://admin:NewPass@192.168.1.100:554/stream1",
    "status": "ACTIVE"
  }'
```

### 5. Xóa camera

```bash
curl -X DELETE http://localhost:8081/api/cameras/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. Toggle camera status

```bash
curl -X PATCH http://localhost:8081/api/cameras/1/toggle-status \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🤖 AI Detection

### 1. Bật AI detection cho một camera

```bash
curl -X POST http://localhost:8081/api/stream-processing/camera/1/start-detection \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "AI detection started successfully",
  "cameraId": 1,
  "detectionRunning": true
}
```

### 2. Dừng AI detection

```bash
curl -X POST http://localhost:8081/api/stream-processing/camera/1/stop-detection \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Bật detection cho tất cả cameras active

```bash
curl -X POST http://localhost:8081/api/stream-processing/start-all-detections \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Started detection for 5 cameras",
  "totalCamerasStarted": 5
}
```

### 4. Dừng tất cả detections

```bash
curl -X POST http://localhost:8081/api/stream-processing/stop-all-detections \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. Kiểm tra trạng thái detection

```bash
curl -X GET http://localhost:8081/api/stream-processing/camera/1/status \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "cameraId": 1,
  "cameraName": "Camera cổng chính",
  "detectionRunning": true,
  "streamActive": true,
  "latestDetections": [
    {
      "objectType": "PERSON",
      "label": "person",
      "confidence": 0.92,
      "boundingBox": {
        "x": 100,
        "y": 150,
        "width": 200,
        "height": 400
      }
    },
    {
      "objectType": "VEHICLE",
      "label": "car",
      "confidence": 0.88,
      "boundingBox": {
        "x": 500,
        "y": 300,
        "width": 300,
        "height": 200
      }
    }
  ]
}
```

### 6. Cấu hình confidence threshold

```bash
curl -X PUT "http://localhost:8081/api/stream-processing/camera/1/confidence-threshold?threshold=0.7" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 7. Cấu hình frame skip

```bash
curl -X PUT "http://localhost:8081/api/stream-processing/camera/1/frame-skip?skipFrames=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📊 AI Events

### 1. Lấy AI events của camera

```bash
curl -X GET "http://localhost:8081/api/ai-events/camera/1?page=0&size=20&sortBy=detectedAt&sortDir=desc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "cameraId": 1,
      "cameraName": "Camera cổng chính",
      "eventType": "OBJECT_DETECTION",
      "detectedAt": "2024-03-12T10:30:45",
      "confidenceScore": 0.85,
      "detectedObjects": [
        {
          "objectType": "PERSON",
          "label": "person",
          "confidence": 0.92,
          "boundingBox": {
            "x": 100,
            "y": 150,
            "width": 200,
            "height": 400
          }
        }
      ]
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

### 2. Lấy thống kê AI events

```bash
curl -X GET "http://localhost:8081/api/ai-events/camera/1/statistics?startDate=2024-03-01&endDate=2024-03-12" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "cameraId": 1,
  "totalEvents": 150,
  "objectCounts": {
    "PERSON": 80,
    "VEHICLE": 70
  },
  "averageConfidence": 0.85,
  "startDate": "2024-03-01",
  "endDate": "2024-03-12"
}
```

### 3. Tìm kiếm AI events theo object type

```bash
curl -X GET "http://localhost:8081/api/ai-events/search?objectType=PERSON&startDate=2024-03-01&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Lấy AI events gần nhất

```bash
curl -X GET "http://localhost:8081/api/ai-events/recent?limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🗺️ Camera Location

### 1. Tạo location

```bash
curl -X POST http://localhost:8081/api/camera-locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Cổng chính",
    "address": "123 Nguyễn Văn Linh, Q7, TP.HCM",
    "latitude": 10.729170,
    "longitude": 106.719023,
    "city": "TP.HCM",
    "district": "Quận 7",
    "description": "Vị trí camera giám sát cổng chính"
  }'
```

### 2. Lấy danh sách locations

```bash
curl -X GET "http://localhost:8081/api/camera-locations?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Thêm camera vào location

```bash
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera cổng chính",
    "code": "CAM-GATE-001",
    "rtspUrl": "rtsp://admin:pass@192.168.1.100:554/stream1",
    "status": "ACTIVE",
    "locationId": 1
  }'
```

---

## 👥 Camera Group

### 1. Tạo group

```bash
curl -X POST http://localhost:8081/api/camera-groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera an ninh",
    "description": "Nhóm camera bảo vệ"
  }'
```

### 2. Thêm camera vào group

```bash
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera cổng chính",
    "code": "CAM-GATE-001",
    "rtspUrl": "rtsp://admin:pass@192.168.1.100:554/stream1",
    "status": "ACTIVE",
    "groupIds": [1, 2]
  }'
```

### 3. Lấy cameras trong group

```bash
curl -X GET "http://localhost:8081/api/camera-groups/1/cameras" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📈 Dashboard & Statistics

### 1. Lấy dashboard data

```bash
curl -X GET http://localhost:8081/api/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "totalCameras": 10,
  "activeCameras": 8,
  "inactiveCameras": 2,
  "totalEvents": 1500,
  "eventsToday": 150,
  "detectionRunning": 5,
  "recentEvents": [...]
}
```

### 2. Lấy thống kê detection theo thời gian

```bash
curl -X GET "http://localhost:8081/api/statistics/detections-by-time?startDate=2024-03-01&endDate=2024-03-12&interval=DAY" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🔔 Health Check

### 1. Kiểm tra health

```bash
curl -X GET http://localhost:8081/actuator/health
```

### 2. Kiểm tra metrics

```bash
curl -X GET http://localhost:8081/actuator/metrics
```

---

## 🐍 Python Script Example

```python
import requests
import time

BASE_URL = "http://localhost:8081/api"

# 1. Login
login_response = requests.post(f"{BASE_URL}/auth/login", json={
    "username": "admin",
    "password": "admin123"
})
token = login_response.json()["token"]

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

# 2. Thêm camera
camera_data = {
    "name": "Camera Test",
    "code": "CAM-TEST-001",
    "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/stream1",
    "status": "ACTIVE",
    "resolution": "1920x1080"
}

camera_response = requests.post(f"{BASE_URL}/cameras", 
                                json=camera_data, 
                                headers=headers)
camera_id = camera_response.json()["id"]
print(f"✅ Camera created with ID: {camera_id}")

# 3. Bật detection
detection_response = requests.post(
    f"{BASE_URL}/stream-processing/camera/{camera_id}/start-detection",
    headers=headers
)
print(f"✅ Detection started: {detection_response.json()}")

# 4. Đợi và kiểm tra kết quả
time.sleep(10)
status_response = requests.get(
    f"{BASE_URL}/stream-processing/camera/{camera_id}/status",
    headers=headers
)
status = status_response.json()
print(f"📊 Detection status: {status['detectionRunning']}")
print(f"🔍 Latest detections: {len(status.get('latestDetections', []))}")

# 5. Lấy AI events
events_response = requests.get(
    f"{BASE_URL}/ai-events/camera/{camera_id}?page=0&size=10",
    headers=headers
)
events = events_response.json()
print(f"📋 Total events: {events['totalElements']}")
```

---

## 🧪 Testing Tips

### 1. Test RTSP URL trước khi add vào hệ thống

```bash
# Dùng FFmpeg
ffplay "rtsp://admin:pass@192.168.1.100:554/stream1"

# Hoặc VLC
vlc "rtsp://admin:pass@192.168.1.100:554/stream1"
```

### 2. Monitor logs khi chạy detection

```bash
tail -f logs/application.log | grep "Detection"
```

### 3. Check performance

```bash
# CPU usage
top -p $(pgrep -f 'java.*camera')

# Memory usage
ps aux | grep java
```

---

## 📞 Support

Nếu cần hỗ trợ:
1. Xem logs: `logs/application.log`
2. Check Swagger UI: `http://localhost:8081/swagger-ui.html`
3. Kiểm tra database connection
4. Verify RTSP URL với FFmpeg/VLC
