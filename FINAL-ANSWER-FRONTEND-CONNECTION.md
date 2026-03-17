# ✅ HOÀN THÀNH: FRONTEND KẾT NỐI CAMERA + YOLO + OPENAI

## 🎯 Câu hỏi của bạn

> "Làm sao để FE có thể kết nối đến camera và yolo, OPEN AI có thể qua đó sử lý đc hành vi"

---

## ✅ Giải pháp đã implement

### 1️⃣ Backend - Video Streaming
✅ **Tạo `VideoStreamController.java`**
- Endpoint: `/api/v1/video/camera/{id}/stream` - MJPEG stream
- Endpoint: `/api/v1/video/camera/{id}/snapshot` - Ảnh hiện tại
- Frontend có thể xem video trực tiếp qua `<img src="...stream" />`

### 2️⃣ Backend - WebSocket Real-time Detection
✅ **Cập nhật `AiDetectionStreamServiceImpl.java`**
- Method `publishDetectionToWebSocket()` - Gửi detections real-time
- Gửi qua topic: `/topic/camera/{id}/stream`
- Data format: `{ cameraId, timestamp, detections: [{label, confidence, boundingBox}] }`

### 3️⃣ Backend - OpenAI Behavior Analysis
✅ **Tạo `AiBehaviorAnalysisController.java`**
- Endpoint: `/api/v1/ai-analysis/analyze-behavior` - Phân tích hành vi
- Endpoint: `/api/v1/ai-analysis/detect-anomaly` - Phát hiện bất thường
- Endpoint: `/api/v1/ai-analysis/describe-scene` - Mô tả cảnh

### 4️⃣ Frontend - Documents
✅ **Tạo documents hướng dẫn chi tiết:**
- `FRONTEND-CONNECTION-SUMMARY.md` - Tóm tắt flow
- `FRONTEND-INTEGRATION-GUIDE.md` - Hướng dẫn đầy đủ với code examples

---

## 📊 Flow Hoàn Chỉnh

```
┌─────────────────┐
│  Camera (RTSP)  │
│  192.168.1.100  │
└────────┬────────┘
         │ rtsp://...
         ▼
┌─────────────────────────────────┐
│      Backend (Spring Boot)      │
│  ┌──────────────────────────┐  │
│  │  VideoStreamService      │  │
│  │  - Capture RTSP frames   │  │
│  └──────────┬───────────────┘  │
│             │                   │
│             ▼                   │
│  ┌──────────────────────────┐  │
│  │  YOLOv8 Detector         │  │
│  │  - Detect người, xe cộ   │  │
│  └──────────┬───────────────┘  │
│             │                   │
│             ▼                   │
│  ┌──────────────────────────┐  │
│  │  WebSocket Publisher     │  │
│  │  - Send detections       │  │
│  └──────────┬───────────────┘  │
│             │                   │
│  ┌──────────┴───────────────┐  │
│  │  (Optional) OpenAI       │  │
│  │  - Analyze behavior      │  │
│  └──────────────────────────┘  │
└─────────────────────────────────┘
         │ HTTP + WebSocket
         ▼
┌─────────────────────────────────┐
│    Frontend (React/Vue)         │
│  ┌──────────────────────────┐  │
│  │  <img src="...stream" /> │  │ ← Xem video
│  └──────────────────────────┘  │
│  ┌──────────────────────────┐  │
│  │  WebSocket.subscribe()   │  │ ← Nhận detections
│  └──────────────────────────┘  │
│  ┌──────────────────────────┐  │
│  │  Canvas overlay          │  │ ← Vẽ bounding boxes
│  └──────────────────────────┘  │
│  ┌──────────────────────────┐  │
│  │  OpenAI button           │  │ ← Phân tích hành vi
│  └──────────────────────────┘  │
└─────────────────────────────────┘
```

---

## 🚀 Cách sử dụng - 5 bước đơn giản

### Bước 1: Start Backend
```bash
mvn spring-boot:run
```

### Bước 2: Thêm Camera RTSP
```bash
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Camera Test",
    "rtspUrl": "rtsp://admin:pass@192.168.1.100:554/stream1",
    "status": "ACTIVE"
  }'
```

### Bước 3: Bật Detection
```bash
curl -X POST http://localhost:8081/api/stream-processing/camera/1/start-detection
```

### Bước 4: Frontend - Xem Video
```jsx
<img 
  src="http://localhost:8081/api/v1/video/camera/1/stream" 
  alt="Camera 1"
/>
```

### Bước 5: Frontend - Nhận Detections
```javascript
// Connect WebSocket
const socket = new SockJS('http://localhost:8081/ws');
const client = new Client({
  webSocketFactory: () => socket,
  onConnect: () => {
    client.subscribe('/topic/camera/1/stream', (message) => {
      const data = JSON.parse(message.body);
      console.log('Detections:', data.detections);
      // data.detections = [
      //   { label: "person", confidence: 0.92, boundingBox: {x, y, width, height} },
      //   { label: "car", confidence: 0.88, boundingBox: {x, y, width, height} }
      // ]
      
      // Vẽ bounding boxes lên canvas
      drawBoundingBoxes(data.detections);
    });
  }
});
client.activate();
```

---

## 📦 Files đã tạo/sửa

### Backend (Java)
1. ✅ `VideoStreamController.java` - Video streaming endpoints
2. ✅ `AiBehaviorAnalysisController.java` - OpenAI integration
3. ✅ `AiDetectionStreamServiceImpl.java` - WebSocket publishing
4. ✅ `ApplicationConfig.java` - RestTemplate bean
5. ✅ `application.properties` - OpenAI config

### Documents
6. ✅ `FRONTEND-CONNECTION-SUMMARY.md` - Tóm tắt (ĐỌC FILE NÀY TRƯỚC!)
7. ✅ `FRONTEND-INTEGRATION-GUIDE.md` - Hướng dẫn chi tiết với code examples
8. ✅ `README.md` - Cập nhật links

---

## 🎨 Frontend Example - Complete Component

```jsx
// CameraView.jsx - Complete working example
import React, { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import axios from 'axios';

function CameraView({ cameraId, token }) {
  const [detections, setDetections] = useState([]);
  const [analysis, setAnalysis] = useState('');
  const canvasRef = useRef(null);
  const imgRef = useRef(null);

  // 1. Connect WebSocket
  useEffect(() => {
    const socket = new SockJS('http://localhost:8081/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        client.subscribe(`/topic/camera/${cameraId}/stream`, (msg) => {
          const data = JSON.parse(msg.body);
          setDetections(data.detections || []);
        });
      }
    });
    client.activate();
    return () => client.deactivate();
  }, [cameraId, token]);

  // 2. Draw bounding boxes
  useEffect(() => {
    const canvas = canvasRef.current;
    const img = imgRef.current;
    if (!canvas || !img) return;

    const ctx = canvas.getContext('2d');
    canvas.width = img.offsetWidth;
    canvas.height = img.offsetHeight;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    detections.forEach(det => {
      const { x, y, width, height } = det.boundingBox;
      
      // Draw rectangle
      ctx.strokeStyle = det.objectType === 'PERSON' ? '#00FF00' : '#0000FF';
      ctx.lineWidth = 3;
      ctx.strokeRect(x, y, width, height);
      
      // Draw label
      ctx.fillStyle = ctx.strokeStyle;
      ctx.fillRect(x, y - 25, 150, 25);
      ctx.fillStyle = '#000';
      ctx.font = '16px Arial';
      ctx.fillText(
        `${det.label} ${(det.confidence * 100).toFixed(0)}%`, 
        x + 5, y - 7
      );
    });
  }, [detections]);

  // 3. Analyze behavior with OpenAI
  const analyzeBehavior = async () => {
    try {
      const res = await axios.post(
        'http://localhost:8081/api/v1/ai-analysis/analyze-behavior',
        {
          cameraId,
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
    } catch (error) {
      console.error('Error analyzing:', error);
    }
  };

  return (
    <div>
      <h2>🎥 Camera {cameraId}</h2>
      
      {/* Video with bounding boxes */}
      <div style={{ position: 'relative', display: 'inline-block' }}>
        <img 
          ref={imgRef}
          src={`http://localhost:8081/api/v1/video/camera/${cameraId}/stream`}
          alt={`Camera ${cameraId}`}
          style={{ width: '100%', maxWidth: '800px' }}
        />
        <canvas 
          ref={canvasRef}
          style={{ position: 'absolute', top: 0, left: 0 }}
        />
      </div>

      {/* Detection info */}
      <div style={{ marginTop: '20px' }}>
        <h3>🤖 Detections: {detections.length}</h3>
        {detections.map((det, i) => (
          <div key={i}>
            {det.label}: {(det.confidence * 100).toFixed(0)}%
          </div>
        ))}
      </div>

      {/* OpenAI Analysis */}
      <button onClick={analyzeBehavior}>
        🧠 Analyze Behavior (OpenAI)
      </button>
      {analysis && (
        <div style={{ background: '#f0f0f0', padding: '10px', marginTop: '10px' }}>
          <strong>AI Analysis:</strong>
          <p>{analysis}</p>
        </div>
      )}
    </div>
  );
}

export default CameraView;
```

---

## 🧠 OpenAI Integration (Optional)

### Enable OpenAI
```properties
# application.properties
ai.openai.enabled=true
ai.openai.api-key=sk-your-key-here
```

### Analyzed Response Example
```
Request:
{
  cameraId: 1,
  detections: [
    { label: "person", confidence: 0.92 },
    { label: "car", confidence: 0.88 }
  ],
  context: "Bank entrance at night"
}

Response:
{
  analysis: "A person is standing near a parked car at the bank entrance. 
             The scene appears normal but requires monitoring as it's nighttime. 
             The person has been stationary for a while which could indicate 
             waiting for someone or potential security concern."
}
```

---

## 📊 Real-time Data Structure

### WebSocket Message từ Backend → Frontend
```javascript
{
  cameraId: 1,
  timestamp: 1709123456789,
  detectionCount: 2,
  detections: [
    {
      objectType: "PERSON",
      label: "person",
      confidence: 0.92,
      boundingBox: {
        x: 150,
        y: 200,
        width: 180,
        height: 350
      }
    },
    {
      objectType: "VEHICLE",
      label: "car",
      confidence: 0.88,
      boundingBox: {
        x: 450,
        y: 280,
        width: 280,
        height: 180
      }
    }
  ]
}
```

---

## ✅ Checklist hoàn thành

- [x] Backend có thể capture RTSP stream
- [x] YOLOv8 phát hiện người và xe cộ
- [x] Backend gửi detections qua WebSocket
- [x] Backend có endpoint video streaming
- [x] Backend có OpenAI behavior analysis
- [x] Frontend có thể xem video stream
- [x] Frontend nhận detections real-time
- [x] Frontend vẽ bounding boxes lên video
- [x] Frontend có thể gọi OpenAI analysis
- [x] Documents đầy đủ với examples

---

## 📚 Tài liệu chi tiết

1. **FRONTEND-CONNECTION-SUMMARY.md** ← BẠN ĐANG ĐỌC FILE NÀY
2. **FRONTEND-INTEGRATION-GUIDE.md** ← Hướng dẫn chi tiết với nhiều examples hơn
3. **REST-API-EXAMPLES.md** ← API reference
4. **OPENAI-INTEGRATION.md** ← OpenAI setup chi tiết

---

## 🎯 Kết luận

Frontend giờ có thể:

✅ **Xem live stream** từ camera RTSP qua MJPEG
✅ **Nhận detections real-time** qua WebSocket từ YOLOv8
✅ **Hiển thị bounding boxes** lên video với Canvas
✅ **Phân tích hành vi** qua OpenAI (optional)

**Flow đã connect hoàn chỉnh từ Camera → YOLOv8 → WebSocket → Frontend → OpenAI!** 🎉

---

## 🚀 Next Steps

1. Start backend: `mvn spring-boot:run`
2. Add camera qua API
3. Start detection
4. Build frontend với code example trên
5. Enjoy! ✨

**Need help? Xem các documents chi tiết hoặc check Swagger UI:** 
`http://localhost:8081/swagger-ui.html`
