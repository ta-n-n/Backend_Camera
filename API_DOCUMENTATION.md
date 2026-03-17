# 📚 API DOCUMENTATION

## 🔗 Cách Frontend Kết Nối với Backend

### 📺 Kiến trúc hiển thị video + AI detection

```
┌─────────────────────────────────────────────────────────┐
│                      BACKEND                            │
│                                                         │
│  RTSP Stream → OpenCV → YOLOv8 → Database              │
│                           ↓                             │
│                    WebSocket Push                       │
└─────────────────────────┬───────────────────────────────┘
                          │
            ┌─────────────┼─────────────┐
            ↓             ↓             ↓
    ┌──────────────┬──────────────┬──────────────┐
    │ Video Stream │  WebSocket   │  REST API    │
    │  (RTSP/HLS)  │  (Realtime)  │  (Control)   │
    └──────────────┴──────────────┴──────────────┘
            ↓             ↓             ↓
    ┌────────────────────────────────────────────────┐
    │              FRONTEND (React/Vue)              │
    │                                                │
    │  Video Player + Bounding Boxes + Alerts        │
    └────────────────────────────────────────────────┘
```

---

## 🎯 3 Cách Kết Nối cho Frontend

### **Cách 1: Frontend tự đọc RTSP** ⭐ Đơn giản nhất

Frontend dùng player hỗ trợ RTSP:
- **WebRTC Player** (jsmpeg, Broadway.js)
- **HLS Player** (Video.js, HLS.js) - nếu convert RTSP → HLS

```jsx
// React example
import ReactPlayer from 'react-player';

function CameraView() {
  return (
    <div>
      {/* Video stream */}
      <ReactPlayer 
        url="rtsp://localhost:8554/camera1"
        playing
        controls
        width="100%"
      />
      
      {/* AI Detection overlay - nhận qua WebSocket */}
      <DetectionOverlay cameraId={1} />
    </div>
  );
}
```

**⚠️ Vấn đề**: Browser không hỗ trợ RTSP trực tiếp.

**✅ Giải pháp**: Dùng WebRTC Gateway (mediamtx hỗ trợ WebRTC)

```jsx
// Kết nối WebRTC
<ReactPlayer 
  url="http://localhost:8889/camera1/whep"
  playing
/>
```

---

### **Cách 2: Backend push snapshots qua WebSocket** ⭐ Recommended

Backend capture frame → encode Base64 → push qua WebSocket.

#### **Backend Code (thêm vào)**

```java
// VideoStreamService thêm method
public void captureAndPushSnapshot(Long cameraId) {
    Mat frame = getCurrentFrame(cameraId);
    if (frame != null) {
        // Encode frame to JPEG
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, mob);
        byte[] imageBytes = mob.toArray();
        
        // Convert to Base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        // Push qua WebSocket
        Map<String, Object> data = Map.of(
            "cameraId", cameraId,
            "image", "data:image/jpeg;base64," + base64Image,
            "timestamp", System.currentTimeMillis()
        );
        webSocketPublisher.publishCameraStreamUpdate(cameraId, data);
    }
}
```

#### **Frontend Code**

```jsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function CameraLiveView({ cameraId }) {
  const [imageData, setImageData] = useState(null);
  const [detections, setDetections] = useState([]);
  
  useEffect(() => {
    // Kết nối WebSocket
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, () => {
      // Subscribe nhận video frames
      stompClient.subscribe(`/topic/camera/${cameraId}/stream`, (message) => {
        const data = JSON.parse(message.body);
        setImageData(data.image);
      });
      
      // Subscribe nhận AI detection events
      stompClient.subscribe(`/topic/camera/${cameraId}/ai-events`, (message) => {
        const event = JSON.parse(message.body);
        setDetections(event.objects); // [{className, x, y, width, height}]
      });
    });
    
    return () => stompClient.disconnect();
  }, [cameraId]);
  
  return (
    <div style={{ position: 'relative' }}>
      {/* Video từ WebSocket */}
      {imageData && <img src={imageData} alt="Camera" style={{ width: '100%' }} />}
      
      {/* Bounding boxes */}
      {detections.map((det, idx) => (
        <div
          key={idx}
          style={{
            position: 'absolute',
            left: det.x,
            top: det.y,
            width: det.width,
            height: det.height,
            border: '2px solid red',
            color: 'red',
            fontSize: '12px',
          }}
        >
          {det.className} {(det.confidence * 100).toFixed(0)}%
        </div>
      ))}
    </div>
  );
}
```

---

### **Cách 3: Backend convert RTSP → HTTP Stream** ⚡ Best Performance

Dùng FFmpeg convert RTSP → HLS/MJPEG HTTP stream.

```bash
# Convert RTSP → HLS
ffmpeg -i rtsp://localhost:8554/camera1 \
  -c:v copy -hls_time 2 -hls_list_size 3 \
  -hls_flags delete_segments \
  /var/www/html/camera1/stream.m3u8
```

Frontend dùng HLS.js:

```jsx
import Hls from 'hls.js';

function CameraHLS({ streamUrl }) {
  const videoRef = useRef();
  
  useEffect(() => {
    const hls = new Hls();
    hls.loadSource('http://localhost/camera1/stream.m3u8');
    hls.attachMedia(videoRef.current);
  }, []);
  
  return <video ref={videoRef} controls autoPlay />;
}
```

---

## 🎥 Stream Processing & AI Detection APIs

Backend đã được tích hợp đầy đủ chức năng xử lý RTSP stream realtime và AI detection.

### Kiến trúc xử lý stream

```
Video File (local) 
    ↓
FFmpeg stream
    ↓
RTSP Server (MediaMTX)
    ↓
Backend (VideoStreamService) → OpenCV VideoCapture
    ↓
Frame Processing → YOLOv8 Detection
    ↓
Save to Database (AiEvent)
    ↓
WebSocket → Frontend Dashboard
```

---

## 🔧 Stream Processing APIs

### 1. Start AI Detection cho một camera

```http
POST /api/v1/stream/detection/start/{cameraId}
```

**Authorization:** `ADMIN`, `OPERATOR`

**Response:**
```json
{
  "success": true,
  "message": "Đã bắt đầu AI detection cho camera Camera 1",
  "data": {
    "cameraId": 1,
    "cameraName": "Camera 1",
    "rtspUrl": "rtsp://localhost:8554/camera1",
    "isRunning": true,
    "status": "RUNNING",
    "message": "Detection started successfully",
    "lastCheckTime": "2026-03-10T14:30:00"
  }
}
```

### 2. Stop AI Detection cho một camera

```http
POST /api/v1/stream/detection/stop/{cameraId}
```

**Authorization:** `ADMIN`, `OPERATOR`

### 3. Start AI Detection cho tất cả cameras active

```http
POST /api/v1/stream/detection/start-all
```

**Authorization:** `ADMIN`

**Response:**
```json
{
  "success": true,
  "message": "Đã bắt đầu AI detection cho 5 cameras",
  "data": "5"
}
```

### 4. Stop tất cả AI Detection

```http
POST /api/v1/stream/detection/stop-all
```

**Authorization:** `ADMIN`

### 5. Cấu hình Detection Parameters

```http
POST /api/v1/stream/detection/config
Content-Type: application/json
```

**Authorization:** `ADMIN`, `OPERATOR`

**Request Body:**
```json
{
  "cameraId": 1,
  "confidenceThreshold": 0.7,
  "frameSkip": 10,
  "autoRestart": true
}
```

**Parameters:**
- `confidenceThreshold`: 0.0 - 1.0 (chỉ lưu detections có confidence >= threshold)
- `frameSkip`: Detect mỗi N frames (tăng lên để giảm CPU usage)
- `autoRestart`: Enable/disable auto-restart khi stream disconnect

### 6. Lấy status của stream

```http
GET /api/v1/stream/status/{cameraId}
```

**Authorization:** `ADMIN`, `OPERATOR`, `VIEWER`

**Response:**
```json
{
  "success": true,
  "data": {
    "cameraId": 1,
    "cameraName": "Camera Parking Lot",
    "rtspUrl": "rtsp://localhost:8554/camera1",
    "isRunning": true,
    "isHealthy": true,
    "status": "RUNNING",
    "lastCheckTime": "2026-03-10T14:35:00"
  }
}
```

### 7. Lấy status của tất cả streams

```http
GET /api/v1/stream/status/all
```

**Authorization:** `ADMIN`, `OPERATOR`, `VIEWER`

### 8. Lấy detections gần nhất

```http
GET /api/v1/stream/detection/latest/{cameraId}
```

**Authorization:** `ADMIN`, `OPERATOR`, `VIEWER`

**Response:**
```json
{
  "success": true,
  "data": {
    "cameraId": 1,
    "cameraName": "Camera Parking Lot",
    "timestamp": "2026-03-10T14:36:00",
    "detections": [
      {
        "className": "person",
        "confidence": 0.89,
        "x": 100,
        "y": 200,
        "width": 50,
        "height": 120
      },
      {
        "className": "car",
        "confidence": 0.92,
        "x": 300,
        "y": 150,
        "width": 200,
        "height": 150
      }
    ],
    "objectCount": 2,
    "averageConfidence": 0.905
  }
}
```

---

## 🔍 Stream Monitoring APIs

### 9. Start Stream Monitoring

```http
POST /api/v1/stream/monitor/start
```

**Authorization:** `ADMIN`

Bật monitoring service để tự động:
- Kiểm tra health của streams mỗi 30 giây
- Auto-restart streams bị disconnect
- Log health status vào database

### 10. Stop Stream Monitoring

```http
POST /api/v1/stream/monitor/stop
```

**Authorization:** `ADMIN`

### 11. Manual Restart Stream

```http
POST /api/v1/stream/restart/{cameraId}
```

**Authorization:** `ADMIN`, `OPERATOR`

Restart stream thủ công khi gặp vấn đề.

### 12. Get Health Status

```http
GET /api/v1/stream/health
```

**Authorization:** `ADMIN`, `OPERATOR`, `VIEWER`

**Response:**
```json
{
  "success": true,
  "data": {
    "1": true,
    "2": true,
    "3": false,
    "4": true
  }
}
```

Key: cameraId, Value: isHealthy

---

## 🚀 Hướng dẫn sử dụng

### Bước 1: Chuẩn bị video file

Lưu video trong máy local (không cần trong project):

```
D:\camera-video\
   traffic.mp4
   parking.mp4
   street.mp4
```

### Bước 2: Chạy RTSP Server (MediaMTX)

```bash
docker run -d --name mediamtx -p 8554:8554 bluenviron/mediamtx
```

### Bước 3: Stream video bằng FFmpeg

```bash
# Windows
ffmpeg -re -stream_loop -1 -i D:\camera-video\traffic.mp4 ^
-c copy -f rtsp rtsp://localhost:8554/camera1

# Linux/Mac
ffmpeg -re -stream_loop -1 -i /home/user/camera-video/traffic.mp4 \
-c copy -f rtsp rtsp://localhost:8554/camera1
```

### Bước 4: Tạo camera trong database

```http
POST /api/v1/cameras
Content-Type: application/json

{
  "name": "Camera Parking Lot",
  "code": "CAM001",
  "rtspUrl": "rtsp://localhost:8554/camera1",
  "status": "ACTIVE",
  "location": {
    "name": "Parking Area A"
  }
}
```

### Bước 5: Start monitoring service

```http
POST /api/v1/stream/monitor/start
```

### Bước 6: Start AI detection

```http
# Single camera
POST /api/v1/stream/detection/start/1

# Hoặc tất cả cameras
POST /api/v1/stream/detection/start-all
```

### Bước 7: Theo dõi detections

```http
# Real-time detections
GET /api/v1/stream/detection/latest/1

# Historical events
GET /api/v1/ai-events?cameraId=1&page=0&size=20
```

---

## ⚙️ Configuration

### application.properties

```properties
# OpenCV native library path (nếu cần)
# opencv.native.lib.path=/usr/local/lib

# Detection settings
ai.detection.confidence.threshold=0.5
ai.detection.frame.skip=5

# Stream monitoring
stream.monitor.check.interval=30000
stream.monitor.auto.restart=true
stream.monitor.max.failures=3
```

---

## 📊 Performance Tips

### 1. Giảm CPU Usage

- Tăng `frameSkip`: Detect mỗi 10-15 frames thay vì mỗi 5 frames
- Giảm resolution của stream nếu có thể
- Limit số cameras chạy đồng thời

### 2. Tăng Detection Accuracy

- Giảm `frameSkip` để detect nhiều hơn
- Tăng `confidenceThreshold` để lọc false positives
- Sử dụng model YOLOv8 lớn hơn (m, l, x thay vì n)

### 3. Stream Stability

- Enable auto-restart
- Monitor health status định kỳ
- Sử dụng RTSP URLs stable (local network tốt hơn internet)

---

## 🐛 Troubleshooting

### Stream không start được

1. Kiểm tra RTSP URL có accessible không:
```bash
curl -v rtsp://localhost:8554/camera1
```

2. Kiểm tra OpenCV đã load được không:
```
Check logs: "OpenCV loaded successfully"
```

3. Kiểm tra camera có rtspUrl valid không

### Detection không lưu vào database

1. Kiểm tra AI model đã load:
```http
GET /api/v1/ai-models
```

2. Kiểm tra confidence threshold (có thể quá cao)

3. Check logs để xem có error không

### Stream bị disconnect liên tục

1. Enable auto-restart:
```http
POST /api/v1/stream/detection/config
{
  "cameraId": 1,
  "autoRestart": true
}
```

2. Kiểm tra RTSP server còn chạy không
3. Kiểm tra network connection

---

## � WebSocket Endpoints

Backend hỗ trợ WebSocket STOMP để push realtime updates.

### Kết nối WebSocket

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  // Authorization (nếu cần)
  'Authorization': 'Bearer ' + token
}, () => {
  console.log('WebSocket connected');
});
```

### Topics có thể subscribe

| Topic | Mô tả | Data format |
|-------|-------|-------------|
| `/topic/alerts` | Tất cả alerts | `AlertResponse` |
| `/topic/ai-events` | Tất cả AI detection events | `AiEventResponse` |
| `/topic/camera/{cameraId}/alerts` | Alerts của camera cụ thể | `AlertResponse` |
| `/topic/camera/{cameraId}/ai-events` | AI events của camera cụ thể | `AiEventResponse` |
| `/topic/camera/{cameraId}/stream` | Video frames/snapshots | `{cameraId, image, timestamp}` |
| `/topic/camera/{cameraId}/status` | Camera status updates | `{cameraId, status, timestamp}` |
| `/topic/camera/{cameraId}/health` | Health monitoring | `{cameraId, isHealthy, ...}` |
| `/topic/system/status` | System status | `{...}` |

### AiEventResponse format

```json
{
  "id": 123,
  "cameraId": 1,
  "cameraName": "Camera 1",
  "eventType": "OBJECT_DETECTED",
  "confidence": 0.89,
  "detectedAt": "2026-03-11T10:30:00",
  "objects": [
    {
      "id": 456,
      "className": "person",
      "confidence": 0.89,
      "boundingBox": {
        "x": 100,
        "y": 200,
        "width": 50,
        "height": 120
      }
    },
    {
      "id": 457,
      "className": "car",
      "confidence": 0.92,
      "boundingBox": {
        "x": 300,
        "y": 150,
        "width": 200,
        "height": 150
      }
    }
  ]
}
```

---

## 📱 Full Frontend Example (React + TypeScript)

### 1. WebSocket Service

```typescript
// services/websocket.service.ts
import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';

class WebSocketService {
  private stompClient: Client | null = null;
  private subscribers = new Map<string, any>();

  connect(token?: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const socket = new SockJS('http://localhost:8080/ws');
      this.stompClient = Stomp.over(socket);

      const headers = token ? { Authorization: `Bearer ${token}` } : {};

      this.stompClient.connect(
        headers,
        () => {
          console.log('✅ WebSocket connected');
          resolve();
        },
        (error: any) => {
          console.error('❌ WebSocket error:', error);
          reject(error);
        }
      );
    });
  }

  subscribe<T>(topic: string, callback: (data: T) => void): void {
    if (!this.stompClient?.connected) {
      console.warn('WebSocket not connected, buffering subscription...');
      return;
    }

    const subscription = this.stompClient.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      callback(data);
    });

    this.subscribers.set(topic, subscription);
  }

  unsubscribe(topic: string): void {
    const subscription = this.subscribers.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.subscribers.delete(topic);
    }
  }

  disconnect(): void {
    if (this.stompClient?.connected) {
      this.stompClient.disconnect(() => {
        console.log('WebSocket disconnected');
      });
    }
  }
}

export default new WebSocketService();
```

### 2. Camera Stream Service

```typescript
// services/camera.service.ts
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/v1';

interface StreamStatusResponse {
  cameraId: number;
  cameraName: string;
  rtspUrl: string;
  isRunning: boolean;
  isHealthy: boolean;
  status: string;
}

export const cameraService = {
  async startDetection(cameraId: number) {
    const response = await axios.post(
      `${API_URL}/stream/detection/start/${cameraId}`
    );
    return response.data;
  },

  async stopDetection(cameraId: number) {
    const response = await axios.post(
      `${API_URL}/stream/detection/stop/${cameraId}`
    );
    return response.data;
  },

  async getStreamStatus(cameraId: number): Promise<StreamStatusResponse> {
    const response = await axios.get(`${API_URL}/stream/status/${cameraId}`);
    return response.data.data;
  },

  async getLatestDetections(cameraId: number) {
    const response = await axios.get(
      `${API_URL}/stream/detection/latest/${cameraId}`
    );
    return response.data.data;
  },

  async configureDetection(config: {
    cameraId: number;
    confidenceThreshold?: number;
    frameSkip?: number;
    autoRestart?: boolean;
  }) {
    const response = await axios.post(
      `${API_URL}/stream/detection/config`,
      config
    );
    return response.data;
  },
};
```

### 3. Camera Live View Component

```tsx
// components/CameraLiveView.tsx
import React, { useEffect, useState, useRef } from 'react';
import websocketService from '../services/websocket.service';
import { cameraService } from '../services/camera.service';

interface Detection {
  className: string;
  confidence: number;
  boundingBox: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
}

interface AiEvent {
  id: number;
  cameraId: number;
  eventType: string;
  objects: Detection[];
}

export const CameraLiveView: React.FC<{ cameraId: number }> = ({ cameraId }) => {
  const [isStreaming, setIsStreaming] = useState(false);
  const [detections, setDetections] = useState<Detection[]>([]);
  const [videoFrame, setVideoFrame] = useState<string>('');
  const videoRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    // Kết nối WebSocket
    websocketService.connect();

    // Subscribe AI detection events
    websocketService.subscribe<AiEvent>(
      `/topic/camera/${cameraId}/ai-events`,
      (event) => {
        console.log('🤖 AI Detection:', event);
        setDetections(event.objects);
      }
    );

    // Subscribe video frames (nếu backend push snapshots)
    websocketService.subscribe<{ image: string }>(
      `/topic/camera/${cameraId}/stream`,
      (data) => {
        setVideoFrame(data.image);
      }
    );

    // Cleanup
    return () => {
      websocketService.unsubscribe(`/topic/camera/${cameraId}/ai-events`);
      websocketService.unsubscribe(`/topic/camera/${cameraId}/stream`);
    };
  }, [cameraId]);

  const handleStartStream = async () => {
    try {
      await cameraService.startDetection(cameraId);
      setIsStreaming(true);
    } catch (error) {
      console.error('Failed to start stream:', error);
    }
  };

  const handleStopStream = async () => {
    try {
      await cameraService.stopDetection(cameraId);
      setIsStreaming(false);
      setDetections([]);
    } catch (error) {
      console.error('Failed to stop stream:', error);
    }
  };

  return (
    <div style={{ position: 'relative', width: '100%', maxWidth: '800px' }}>
      {/* Controls */}
      <div style={{ marginBottom: '10px' }}>
        <button onClick={handleStartStream} disabled={isStreaming}>
          ▶️ Start AI Detection
        </button>
        <button onClick={handleStopStream} disabled={!isStreaming}>
          ⏹️ Stop
        </button>
      </div>

      {/* Video Display */}
      <div style={{ position: 'relative', backgroundColor: '#000' }}>
        {videoFrame ? (
          <img
            ref={videoRef}
            src={videoFrame}
            alt="Camera Stream"
            style={{ width: '100%', display: 'block' }}
          />
        ) : (
          <div style={{ padding: '100px', textAlign: 'center', color: '#fff' }}>
            {isStreaming ? 'Waiting for video...' : 'Stream stopped'}
          </div>
        )}

        {/* Bounding Boxes Overlay */}
        {detections.map((det, idx) => (
          <div
            key={idx}
            style={{
              position: 'absolute',
              left: det.boundingBox.x,
              top: det.boundingBox.y,
              width: det.boundingBox.width,
              height: det.boundingBox.height,
              border: '3px solid #ff0000',
              backgroundColor: 'rgba(255, 0, 0, 0.1)',
              pointerEvents: 'none',
            }}
          >
            <span
              style={{
                position: 'absolute',
                top: '-20px',
                left: 0,
                backgroundColor: '#ff0000',
                color: '#fff',
                padding: '2px 6px',
                fontSize: '12px',
                borderRadius: '3px',
                whiteSpace: 'nowrap',
              }}
            >
              {det.className} {(det.confidence * 100).toFixed(0)}%
            </span>
          </div>
        ))}
      </div>

      {/* Detection Stats */}
      <div style={{ marginTop: '10px', fontSize: '14px' }}>
        <strong>Detections:</strong> {detections.length} objects
        {detections.length > 0 && (
          <ul style={{ margin: '5px 0', paddingLeft: '20px' }}>
            {detections.map((det, idx) => (
              <li key={idx}>
                {det.className} - {(det.confidence * 100).toFixed(1)}%
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};
```

### 4. Usage trong App

```tsx
// App.tsx
import { CameraLiveView } from './components/CameraLiveView';

function App() {
  return (
    <div>
      <h1>🎥 Camera Surveillance System</h1>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        <CameraLiveView cameraId={1} />
        <CameraLiveView cameraId={2} />
      </div>
    </div>
  );
}
```

---

## 📝 Notes

- Video files chỉ cần lưu trên máy chạy FFmpeg (dev machine hoặc server)
- Backend **KHÔNG** lưu video files, chỉ lưu RTSP URLs
- AI detections được lưu vào `ai_events` và `ai_event_objects` tables
- Stream monitoring tự động ghi logs vào `camera_health_logs` table
- Cleanup old health logs tự động mỗi 1 giờ (giữ 7 ngày gần nhất)
- **WebSocket reconnection**: Client nên implement auto-reconnect khi connection lost
- **Video display**: Backend hiện tại chưa push snapshots, cần implement nếu dùng Cách 2

---

## 📞 Support

Nếu có vấn đề, kiểm tra:
1. Application logs
2. Camera health logs: `GET /api/v1/camera-health-logs?cameraId=1`
3. Stream status: `GET /api/v1/stream/status/all`
