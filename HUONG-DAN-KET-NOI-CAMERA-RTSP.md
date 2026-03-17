# 📹 HƯỚNG DẪN KẾT NỐI CAMERA RTSP THẬT

## 🎯 Tổng quan

Hệ thống hỗ trợ kết nối camera thật qua giao thức RTSP với AI detection (YOLOv8) để phát hiện:
- 👤 **Người** (person)
- 🚗 **Xe cộ** (car, motorcycle, bus, truck, train)
- 🚲 **Xe đạp** (bicycle)

---

## 🔧 Chuẩn bị

### 1. Camera RTSP

Đảm bảo camera của bạn hỗ trợ RTSP. Các hãng camera phổ biến:
- **Hikvision**: `rtsp://username:password@ip:554/Streaming/Channels/101`
- **Dahua**: `rtsp://username:password@ip:554/cam/realmonitor?channel=1&subtype=0`
- **TP-Link**: `rtsp://username:password@ip:554/stream1`
- **Generic RTSP**: `rtsp://username:password@ip:port/path`

### 2. Kiểm tra kết nối RTSP

Sử dụng VLC Media Player:
```
1. Mở VLC
2. Media > Open Network Stream
3. Nhập RTSP URL
4. Play để kiểm tra
```

Hoặc dùng FFmpeg:
```bash
ffplay "rtsp://username:password@192.168.1.100:554/stream1"
```

---

## 🚀 Thêm Camera qua REST API

### 1. Tạo Location (Vị trí camera)

**Endpoint:** `POST /api/camera-locations`

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
    "description": "Camera giám sát cổng ra vào chính"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Cổng chính",
  "address": "123 Nguyễn Văn Linh, Q7, TP.HCM",
  ...
}
```

### 2. Tạo Camera Group (nhóm camera)

**Endpoint:** `POST /api/camera-groups`

```bash
curl -X POST http://localhost:8081/api/camera-groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera an ninh",
    "description": "Hệ thống camera bảo vệ"
  }'
```

### 3. Thêm Camera với RTSP URL

**Endpoint:** `POST /api/cameras`

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
    "status": "ACTIVE",
    "resolution": "1920x1080",
    "frameRate": 30,
    "locationId": 1,
    "groupIds": [1],
    "description": "Camera giám sát cổng ra vào chính"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Camera cổng chính",
  "code": "CAM-GATE-001",
  "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/Streaming/Channels/101",
  "status": "ACTIVE",
  ...
}
```

---

## 🤖 Bật AI Detection

### 1. Bật detection cho một camera

**Endpoint:** `POST /api/stream-processing/camera/{cameraId}/start-detection`

```bash
curl -X POST http://localhost:8081/api/stream-processing/camera/1/start-detection \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "AI detection started for camera 1",
  "cameraId": 1,
  "detectionRunning": true
}
```

### 2. Bật detection cho tất cả cameras active

**Endpoint:** `POST /api/stream-processing/start-all-detections`

```bash
curl -X POST http://localhost:8081/api/stream-processing/start-all-detections \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Kiểm tra trạng thái detection

**Endpoint:** `GET /api/stream-processing/camera/{cameraId}/status`

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
    }
  ]
}
```

### 4. Dừng detection

**Endpoint:** `POST /api/stream-processing/camera/{cameraId}/stop-detection`

```bash
curl -X POST http://localhost:8081/api/stream-processing/camera/1/stop-detection \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📊 Xem Kết quả Detection

### 1. Lấy AI events của camera

**Endpoint:** `GET /api/ai-events/camera/{cameraId}`

```bash
curl -X GET "http://localhost:8081/api/ai-events/camera/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. Lấy thống kê detection

**Endpoint:** `GET /api/ai-events/camera/{cameraId}/statistics`

```bash
curl -X GET http://localhost:8081/api/ai-events/camera/1/statistics \
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
  "averageConfidence": 0.85
}
```

---

## ⚙️ Cấu hình Detection

### 1. Config trong application.properties

```properties
# Bật/tắt filter detection
ai.yolo.detection-filter.enable-filter=true

# Chỉ phát hiện người và xe cộ
ai.yolo.detection-filter.allowed-classes=person,bicycle,car,motorcycle,bus,train,truck

# Ngưỡng confidence (0.0 - 1.0)
ai.yolo.confidence.threshold=0.5

# Frame skip (phát hiện mỗi N frames để giảm CPU)
ai.video.fps=5
```

### 2. Thay đổi confidence threshold runtime

**Endpoint:** `PUT /api/stream-processing/camera/{cameraId}/confidence-threshold`

```bash
curl -X PUT "http://localhost:8081/api/stream-processing/camera/1/confidence-threshold?threshold=0.7" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🔍 Troubleshooting

### Camera không kết nối được

1. **Kiểm tra RTSP URL:**
   ```bash
   ffplay "rtsp://username:password@ip:554/stream1"
   ```

2. **Kiểm tra firewall:**
   - Port 554 (RTSP) phải được mở
   - Ping camera IP để kiểm tra network

3. **Kiểm tra authentication:**
   - Username/password đúng
   - Camera có enable RTSP không

### Detection không hoạt động

1. **Kiểm tra model:**
   ```bash
   ls models/yolov8n.onnx
   ```

2. **Xem logs:**
   ```bash
   tail -f logs/application.log | grep "Detection"
   ```

3. **Kiểm tra CPU/Memory:**
   - Detection cần ~2GB RAM
   - CPU ít nhất 4 cores

---

## 📝 Ví dụ hoàn chỉnh

### Script Python để thêm camera

```python
import requests

BASE_URL = "http://localhost:8081/api"
TOKEN = "your_jwt_token"

headers = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {TOKEN}"
}

# 1. Tạo location
location_data = {
    "name": "Cổng chính",
    "address": "123 Nguyễn Văn Linh, Q7, TP.HCM",
    "latitude": 10.729170,
    "longitude": 106.719023,
    "city": "TP.HCM",
    "district": "Quận 7"
}
location_resp = requests.post(f"{BASE_URL}/camera-locations", 
                              json=location_data, headers=headers)
location_id = location_resp.json()["id"]

# 2. Tạo camera
camera_data = {
    "name": "Camera cổng chính",
    "code": "CAM-GATE-001",
    "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/stream1",
    "status": "ACTIVE",
    "locationId": location_id
}
camera_resp = requests.post(f"{BASE_URL}/cameras", 
                           json=camera_data, headers=headers)
camera_id = camera_resp.json()["id"]

# 3. Bật detection
detection_resp = requests.post(
    f"{BASE_URL}/stream-processing/camera/{camera_id}/start-detection",
    headers=headers
)

print(f"✅ Camera {camera_id} đã được thêm và bật detection!")
```

---

## 🎓 Lưu ý

1. **Không cần tạo camera ảo:**
   - Không chạy với `--spring.profiles.active=seed`
   - Chỉ thêm camera thật qua API

2. **Performance:**
   - Mỗi camera detection tốn ~500MB RAM
   - CPU tăng ~20% mỗi camera
   - Khuyến nghị: Max 10 cameras/server

3. **Security:**
   - Luôn dùng HTTPS trong production
   - Không hardcode password trong code
   - Sử dụng environment variables

4. **Monitoring:**
   - Kiểm tra health endpoint: `GET /actuator/health`
   - Xem metrics: `GET /actuator/metrics`

---

## 📞 Liên hệ

Nếu cần hỗ trợ, vui lòng tạo issue trên GitHub hoặc liên hệ qua email.
