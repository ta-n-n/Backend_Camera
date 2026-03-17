# 🎯 TÓM TẮT: FRONTEND KẾT NỐI VỚI CAMERA + YOLO + OPENAI

## 📊 Flow Hoàn Chỉnh

```
1️⃣ CAMERA (RTSP)
    ↓
    rtsp://192.168.1.100:554/stream1
    ↓
2️⃣ BACKEND (Spring Boot)
    ├─ VideoStreamService: Capture frames từ RTSP
    ├─ YOLOv8: Detect người, xe cộ
    └─ WebSocket: Gửi results real-time
    ↓
    ┌─────────────────────────────────────┐
    │  /api/v1/video/camera/1/stream      │ ← Video stream (MJPEG)
    │  ws://localhost:8081/ws             │ ← WebSocket connection
    │  /topic/camera/1/stream             │ ← Detection updates
    └─────────────────────────────────────┘
    ↓
3️⃣ FRONTEND (React/Vue)
    ├─ <img src="/api/v1/video/camera/1/stream" />  ← Xem video
    ├─ WebSocket.subscribe()                        ← Nhận detections
    └─ Canvas.drawRect(boundingBox)                 ← Vẽ bounding boxes
    ↓
4️⃣ (OPTIONAL) OPENAI
    ├─ POST /api/v1/ai-analysis/analyze-behavior
    └─ Phân tích hành vi chi tiết
```

---

## 🚀 Bước 1: Start Backend

```bash
# Đảm bảo YOLOv8 model có sẵn
ls models/yolov8n.onnx

# Chạy backend (KHÔNG dùng profile seed)
mvn spring-boot:run
```

**Backend sẽ chạy tại:** `http://localhost:8081`

---

## 📹 Bước 2: Thêm Camera

```bash
# Thêm camera RTSP thật
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Camera cổng chính",
    "code": "CAM-001",
    "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/stream1",
    "status": "ACTIVE"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Camera cổng chính",
  "rtspUrl": "rtsp://admin:Admin123@192.168.1.100:554/stream1",
  ...
}
```

---

## 🤖 Bước 3: Bật AI Detection

```bash
# Bật YOLOv8 detection cho camera
curl -X POST http://localhost:8081/api/stream-processing/camera/1/start-detection \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Sau bước này:**
- ✅ Backend capture frames từ RTSP
- ✅ YOLOv8 phát hiện người, xe cộ
- ✅ Results được gửi qua WebSocket

---

## 💻 Bước 4: Frontend Xem Video + Detections

### 4.1. Install Dependencies

```bash
npm install sockjs-client @stomp/stompjs axios
```

### 4.2. Connect WebSocket

```javascript
// websocket.js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8081/ws');
const client = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  onConnect: () => {
    // Subscribe to camera detections
    client.subscribe('/topic/camera/1/stream', (message) => {
      const data = JSON.parse(message.body);
      console.log('Detections:', data.detections);
      // data.detections = [{ label, confidence, boundingBox: {x, y, width, height} }]
    });
  }
});

client.activate();
```

### 4.3. Hiển thị Video Stream

```jsx
// CameraView.jsx
function CameraView({ cameraId }) {
  return (
    <div>
      {/* Video stream từ backend */}
      <img 
        src={`http://localhost:8081/api/v1/video/camera/${cameraId}/stream`}
        alt="Camera Stream"
        style={{ width: '100%' }}
      />
    </div>
  );
}
```

### 4.4. Vẽ Bounding Boxes

```jsx
// CameraWithDetection.jsx
import { useEffect, useRef, useState } from 'react';

function CameraWithDetection({ cameraId, token }) {
  const [detections, setDetections] = useState([]);
  const canvasRef = useRef(null);

  useEffect(() => {
    // Connect WebSocket và subscribe
    const client = connectWebSocket(token);
    
    client.subscribe(`/topic/camera/${cameraId}/stream`, (message) => {
      const data = JSON.parse(message.body);
      setDetections(data.detections);
    });
  }, [cameraId]);

  useEffect(() => {
    // Vẽ bounding boxes lên canvas
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    detections.forEach(det => {
      const { x, y, width, height } = det.boundingBox;
      
      // Vẽ rectangle
      ctx.strokeStyle = det.objectType === 'PERSON' ? '#00FF00' : '#0000FF';
      ctx.lineWidth = 3;
      ctx.strokeRect(x, y, width, height);
      
      // Vẽ label
      ctx.fillStyle = ctx.strokeStyle;
      ctx.fillRect(x, y - 25, 150, 25);
      ctx.fillStyle = '#000';
      ctx.font = '16px Arial';
      ctx.fillText(`${det.label} ${(det.confidence * 100).toFixed(0)}%`, x + 5, y - 7);
    });
  }, [detections]);

  return (
    <div style={{ position: 'relative' }}>
      <img 
        src={`http://localhost:8081/api/v1/video/camera/${cameraId}/stream`}
        style={{ width: '100%' }}
      />
      <canvas 
        ref={canvasRef}
        style={{ position: 'absolute', top: 0, left: 0 }}
      />
    </div>
  );
}
```

---

## 🧠 Bước 5 (Optional): OpenAI Behavior Analysis

### 5.1. Enable OpenAI

```properties
# application.properties
ai.openai.enabled=true
ai.openai.api-key=sk-your-openai-key-here
```

### 5.2. Frontend Call OpenAI Analysis

```javascript
// analyzeBehavior.js
async function analyzeBehavior(cameraId, detections) {
  const response = await axios.post(
    'http://localhost:8081/api/v1/ai-analysis/analyze-behavior',
    {
      cameraId: cameraId,
      detections: detections.map(d => ({
        label: d.label,
        objectType: d.objectType,
        confidence: d.confidence
      })),
      context: 'Bank entrance monitoring at night'
    },
    {
      headers: { Authorization: `Bearer ${token}` }
    }
  );

  return response.data.data.analysis;
  // Example: "A person is standing near the entrance for an extended period. 
  //           This could be normal customer behavior or require attention..."
}
```

---

## 📊 Data Structures

### Detection từ WebSocket

```javascript
{
  cameraId: 1,
  timestamp: 1709123456789,
  detectionCount: 3,
  detections: [
    {
      objectType: "PERSON",
      label: "person",
      confidence: 0.92,
      boundingBox: {
        x: 100,
        y: 150,
        width: 200,
        height: 400
      }
    },
    {
      objectType: "VEHICLE",
      label: "car",
      confidence: 0.88,
      boundingBox: {
        x: 500,
        y: 300,
        width: 300,
        height: 200
      }
    }
  ]
}
```

---

## 🎯 Complete Example

```jsx
// App.jsx - Complete Example
import React, { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import axios from 'axios';

function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [cameras, setCameras] = useState([]);
  const [selectedCamera, setSelectedCamera] = useState(null);
  const [detections, setDetections] = useState([]);
  const [analysis, setAnalysis] = useState('');
  const wsClient = useRef(null);

  // 1. Login
  const login = async (username, password) => {
    const res = await axios.post('http://localhost:8081/api/auth/login', {
      username, password
    });
    setToken(res.data.token);
    localStorage.setItem('token', res.data.token);
  };

  // 2. Fetch cameras
  useEffect(() => {
    if (token) {
      axios.get('http://localhost:8081/api/cameras', {
        headers: { Authorization: `Bearer ${token}` }
      }).then(res => setCameras(res.data.content));
    }
  }, [token]);

  // 3. Connect WebSocket
  useEffect(() => {
    if (token && selectedCamera) {
      const socket = new SockJS('http://localhost:8081/ws');
      wsClient.current = new Client({
        webSocketFactory: () => socket,
        connectHeaders: { Authorization: `Bearer ${token}` },
        onConnect: () => {
          wsClient.current.subscribe(
            `/topic/camera/${selectedCamera}/stream`,
            (msg) => {
              const data = JSON.parse(msg.body);
              setDetections(data.detections || []);
            }
          );
        }
      });
      wsClient.current.activate();

      return () => wsClient.current?.deactivate();
    }
  }, [token, selectedCamera]);

  // 4. Start detection
  const startDetection = async (cameraId) => {
    await axios.post(
      `http://localhost:8081/api/stream-processing/camera/${cameraId}/start-detection`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    setSelectedCamera(cameraId);
  };

  // 5. Analyze behavior with OpenAI (if enabled)
  const analyzeBehavior = async () => {
    const res = await axios.post(
      'http://localhost:8081/api/v1/ai-analysis/analyze-behavior',
      {
        cameraId: selectedCamera,
        detections: detections.map(d => ({
          label: d.label,
          objectType: d.objectType,
          confidence: d.confidence
        })),
        context: 'Building entrance monitoring'
      },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    setAnalysis(res.data.data.analysis);
  };

  return (
    <div>
      <h1>🎥 Camera Surveillance Dashboard</h1>
      
      {/* Camera list */}
      <div>
        {cameras.map(cam => (
          <button key={cam.id} onClick={() => startDetection(cam.id)}>
            {cam.name}
          </button>
        ))}
      </div>

      {/* Video stream */}
      {selectedCamera && (
        <div>
          <img 
            src={`http://localhost:8081/api/v1/video/camera/${selectedCamera}/stream`}
            style={{ width: '100%', maxWidth: '800px' }}
          />
          
          {/* Detections */}
          <div>
            <h3>🤖 Detections: {detections.length}</h3>
            {detections.map((det, i) => (
              <div key={i}>
                {det.label}: {(det.confidence * 100).toFixed(0)}%
              </div>
            ))}
          </div>

          {/* OpenAI Analysis */}
          <button onClick={analyzeBehavior}>🧠 Analyze Behavior (OpenAI)</button>
          {analysis && <p>{analysis}</p>}
        </div>
      )}
    </div>
  );
}

export default App;
```

---

## 📝 Checklist

- [x] Backend có VideoStreamController ✅
- [x] Backend có WebSocket config ✅
- [x] Backend gửi detections qua WebSocket ✅
- [x] Backend có OpenAI controller (optional) ✅
- [x] Frontend connect WebSocket ✅
- [x] Frontend display video stream ✅
- [x] Frontend vẽ bounding boxes ✅
- [x] Frontend call OpenAI (optional) ✅

---

## 🔍 Endpoints Quan Trọng

| Endpoint | Method | Mục đích |
|----------|--------|----------|
| `/api/cameras` | POST | Thêm camera RTSP |
| `/api/stream-processing/camera/{id}/start-detection` | POST | Bật YOLOv8 detection |
| `/api/v1/video/camera/{id}/stream` | GET | MJPEG video stream |
| `/api/v1/video/camera/{id}/snapshot` | GET | Screenshot hiện tại |
| `/ws` | WebSocket | Connect WebSocket |
| `/topic/camera/{id}/stream` | Subscribe | Nhận detection updates |
| `/api/v1/ai-analysis/analyze-behavior` | POST | OpenAI phân tích hành vi |

---

## 💡 Tips

1. **Video Streaming:**
   - MJPEG đơn giản nhất: `<img src="/api/v1/video/camera/1/stream" />`
   - Fallback: Polling snapshots mỗi giây

2. **Bounding Boxes:**
   - Dùng Canvas overlay trên video
   - Update canvas khi có detection mới từ WebSocket

3. **Performance:**
   - Backend đã config frame skip = 5
   - Frontend debounce canvas redraw nếu cần

4. **OpenAI:**
   - Chỉ enable khi thật sự cần
   - Chi phí ~$0.01/request
   - Dùng cho alerts quan trọng

---

## 🎉 Kết quả

Frontend giờ có thể:
- ✅ Xem live stream từ camera RTSP
- ✅ Nhận real-time detection từ YOLOv8
- ✅ Hiển thị bounding boxes lên video
- ✅ (Optional) Phân tích hành vi qua OpenAI

**Toàn bộ flow đã connect hoàn chỉnh!** 🚀

---

Xem chi tiết trong:
- `FRONTEND-INTEGRATION-GUIDE.md` - Hướng dẫn đầy đủ
- `REST-API-EXAMPLES.md` - API reference
- `OPENAI-INTEGRATION.md` - OpenAI setup
