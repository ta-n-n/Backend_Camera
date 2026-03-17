# 🎨 FRONTEND INTEGRATION GUIDE

## 📋 Tổng quan

Hướng dẫn Frontend kết nối với hệ thống Camera Surveillance để:
1. 📹 **Xem live stream từ camera**
2. 🤖 **Nhận real-time detection từ YOLOv8** (bounding boxes)
3. 🔔 **Nhận alerts và notifications**
4. 🧠 **(Optional) Phân tích hành vi qua OpenAI**

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────┐
│   Camera    │ RTSP
│   (IP Cam)  │────────┐
└─────────────┘        │
                       ▼
                ┌──────────────┐
                │   Backend    │
                │   (Java)     │
                │              │
                │  - YOLOv8    │◄──── REST API
                │  - OpenCV    │
                │  - OpenAI    │◄──── WebSocket
                └──────────────┘
                       │
                       │ HTTP + WS
                       ▼
                ┌──────────────┐
                │   Frontend   │
                │ (React/Vue)  │
                │              │
                │  - Video     │
                │  - Bboxes    │
                │  - Alerts    │
                └──────────────┘
```

---

## 🔌 1. WebSocket Connection

### Setup WebSocket Client

#### React with SockJS + Stomp

```bash
npm install sockjs-client @stomp/stompjs
```

```javascript
// src/services/websocket.js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
  }

  connect(token) {
    return new Promise((resolve, reject) => {
      const socket = new SockJS('http://localhost:8081/ws');
      
      this.client = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => console.log('STOMP:', str),
        
        onConnect: () => {
          console.log('✅ WebSocket connected');
          this.connected = true;
          resolve();
        },
        
        onStompError: (frame) => {
          console.error('❌ STOMP error:', frame);
          reject(frame);
        }
      });

      this.client.activate();
    });
  }

  // Subscribe to camera detection updates
  subscribeToCamera(cameraId, callback) {
    if (!this.connected) {
      throw new Error('WebSocket not connected');
    }

    return this.client.subscribe(
      `/topic/camera/${cameraId}/stream`,
      (message) => {
        const data = JSON.parse(message.body);
        callback(data);
      }
    );
  }

  // Subscribe to AI events
  subscribeToAiEvents(cameraId, callback) {
    return this.client.subscribe(
      `/topic/camera/${cameraId}/ai-events`,
      (message) => {
        const event = JSON.parse(message.body);
        callback(event);
      }
    );
  }

  // Subscribe to alerts
  subscribeToAlerts(callback) {
    return this.client.subscribe(
      '/topic/alerts',
      (message) => {
        const alert = JSON.parse(message.body);
        callback(alert);
      }
    );
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
    }
  }
}

export default new WebSocketService();
```

---

## 📹 2. Video Streaming

### Method 1: MJPEG Stream (Đơn giản nhất)

```jsx
// React Component
import React from 'react';

function CameraStream({ cameraId }) {
  const streamUrl = `http://localhost:8081/api/v1/video/camera/${cameraId}/stream`;
  
  return (
    <div className="camera-container">
      <img 
        src={streamUrl}
        alt={`Camera ${cameraId}`}
        style={{ width: '100%', height: 'auto' }}
        onError={(e) => {
          console.error('Stream error');
          e.target.src = '/placeholder.jpg';
        }}
      />
    </div>
  );
}

export default CameraStream;
```

### Method 2: Snapshot Polling (Fallback)

```jsx
import React, { useState, useEffect, useRef } from 'react';

function CameraSnapshot({ cameraId, refreshRate = 1000 }) {
  const [imageSrc, setImageSrc] = useState('');
  const imgRef = useRef(null);

  useEffect(() => {
    const fetchSnapshot = async () => {
      try {
        const url = `http://localhost:8081/api/v1/video/camera/${cameraId}/snapshot?t=${Date.now()}`;
        setImageSrc(url);
      } catch (error) {
        console.error('Error fetching snapshot:', error);
      }
    };

    fetchSnapshot();
    const interval = setInterval(fetchSnapshot, refreshRate);

    return () => clearInterval(interval);
  }, [cameraId, refreshRate]);

  return (
    <img 
      ref={imgRef}
      src={imageSrc} 
      alt={`Camera ${cameraId}`}
      style={{ width: '100%' }}
    />
  );
}

export default CameraSnapshot;
```

---

## 🤖 3. Real-time Detection Display

### Complete Camera Component with Detections

```jsx
// CameraWithDetection.jsx
import React, { useState, useEffect, useRef } from 'react';
import WebSocketService from '../services/websocket';

function CameraWithDetection({ cameraId, token }) {
  const [detections, setDetections] = useState([]);
  const [videoSize, setVideoSize] = useState({ width: 0, height: 0 });
  const imgRef = useRef(null);
  const canvasRef = useRef(null);

  useEffect(() => {
    // Connect WebSocket
    WebSocketService.connect(token).then(() => {
      // Subscribe to detection updates
      const subscription = WebSocketService.subscribeToCamera(
        cameraId,
        handleDetectionUpdate
      );

      return () => subscription.unsubscribe();
    });
  }, [cameraId, token]);

  const handleDetectionUpdate = (data) => {
    console.log('Detection update:', data);
    setDetections(data.detections || []);
  };

  const handleImageLoad = () => {
    if (imgRef.current) {
      setVideoSize({
        width: imgRef.current.offsetWidth,
        height: imgRef.current.offsetHeight
      });
    }
  };

  // Draw bounding boxes on canvas
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !videoSize.width) return;

    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Set canvas size to match image
    canvas.width = videoSize.width;
    canvas.height = videoSize.height;

    // Draw each detection
    detections.forEach(detection => {
      const bbox = detection.boundingBox;
      if (!bbox) return;

      // Colors by object type
      const colors = {
        'PERSON': '#00FF00',
        'VEHICLE': '#0000FF',
        'ANIMAL': '#FF0000'
      };
      const color = colors[detection.objectType] || '#FFFFFF';

      // Draw rectangle
      ctx.strokeStyle = color;
      ctx.lineWidth = 3;
      ctx.strokeRect(bbox.x, bbox.y, bbox.width, bbox.height);

      // Draw label background
      ctx.fillStyle = color;
      ctx.fillRect(bbox.x, bbox.y - 25, 150, 25);

      // Draw label text
      ctx.fillStyle = '#000000';
      ctx.font = '16px Arial';
      const label = `${detection.label} ${(detection.confidence * 100).toFixed(0)}%`;
      ctx.fillText(label, bbox.x + 5, bbox.y - 7);
    });
  }, [detections, videoSize]);

  const streamUrl = `http://localhost:8081/api/v1/video/camera/${cameraId}/stream`;

  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <img
        ref={imgRef}
        src={streamUrl}
        alt={`Camera ${cameraId}`}
        onLoad={handleImageLoad}
        style={{ width: '100%', display: 'block' }}
      />
      <canvas
        ref={canvasRef}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          pointerEvents: 'none'
        }}
      />
      
      {/* Detection info panel */}
      <div style={{
        position: 'absolute',
        bottom: 10,
        right: 10,
        background: 'rgba(0,0,0,0.7)',
        color: 'white',
        padding: '10px',
        borderRadius: '5px'
      }}>
        <div>🎯 Detections: {detections.length}</div>
        {detections.map((det, idx) => (
          <div key={idx}>
            {det.label}: {(det.confidence * 100).toFixed(0)}%
          </div>
        ))}
      </div>
    </div>
  );
}

export default CameraWithDetection;
```

---

## 📊 4. Dashboard với Multiple Cameras

```jsx
// Dashboard.jsx
import React, { useState, useEffect } from 'react';
import CameraWithDetection from './CameraWithDetection';
import axios from 'axios';

function Dashboard() {
  const [cameras, setCameras] = useState([]);
  const [token, setToken] = useState(localStorage.getItem('token'));

  useEffect(() => {
    fetchCameras();
  }, []);

  const fetchCameras = async () => {
    try {
      const response = await axios.get('http://localhost:8081/api/cameras', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCameras(response.data.content || []);
    } catch (error) {
      console.error('Error fetching cameras:', error);
    }
  };

  const startDetection = async (cameraId) => {
    try {
      await axios.post(
        `http://localhost:8081/api/stream-processing/camera/${cameraId}/start-detection`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert('Detection started!');
    } catch (error) {
      console.error('Error starting detection:', error);
    }
  };

  return (
    <div className="dashboard">
      <h1>Camera Surveillance Dashboard</h1>
      
      <div className="camera-grid" style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
        gap: '20px',
        padding: '20px'
      }}>
        {cameras.map(camera => (
          <div key={camera.id} className="camera-card">
            <h3>{camera.name}</h3>
            <CameraWithDetection 
              cameraId={camera.id} 
              token={token}
            />
            <button onClick={() => startDetection(camera.id)}>
              Start Detection
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Dashboard;
```

---

## 🧠 5. OpenAI Behavior Analysis (Optional)

### Backend Service (Thêm vào dự án)

```java
// OpenAIBehaviorAnalysisService.java
package com.example.camerasurveillancesystem.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
public class OpenAIBehaviorAnalysisService {

    @Value("${ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeBehavior(List<DetectionResult> detections, String context) {
        // Build prompt from detections
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this surveillance scene:\\n");
        prompt.append("Context: ").append(context).append("\\n");
        prompt.append("Detected objects:\\n");
        
        for (DetectionResult det : detections) {
            prompt.append("- ").append(det.getLabel())
                  .append(" (confidence: ").append(det.getConfidence())
                  .append(")\\n");
        }
        
        prompt.append("\\nIs there any suspicious or unusual behavior? Describe what's happening.");

        // Call OpenAI API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4",
            "messages", List.of(
                Map.of("role", "system", "content", "You are a security analyst."),
                Map.of("role", "user", "content", prompt.toString())
            ),
            "max_tokens", 300
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
            );
            
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List) body.get("choices");
            Map<String, Object> message = (Map) choices.get(0).get("message");
            
            return (String) message.get("content");
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            return null;
        }
    }
}
```

### Frontend - Request Behavior Analysis

```jsx
// BehaviorAnalysis.jsx
import React, { useState } from 'react';
import axios from 'axios';

function BehaviorAnalysis({ cameraId, detections }) {
  const [analysis, setAnalysis] = useState('');
  const [loading, setLoading] = useState(false);

  const analyzeBehavior = async () => {
    setLoading(true);
    try {
      const response = await axios.post(
        `http://localhost:8081/api/ai/analyze-behavior`,
        {
          cameraId: cameraId,
          detections: detections,
          context: 'Bank entrance monitoring at night'
        },
        {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        }
      );
      
      setAnalysis(response.data.analysis);
    } catch (error) {
      console.error('Error analyzing behavior:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="behavior-analysis">
      <button onClick={analyzeBehavior} disabled={loading}>
        {loading ? '🤖 Analyzing...' : '🧠 Analyze Behavior (OpenAI)'}
      </button>
      
      {analysis && (
        <div className="analysis-result">
          <h4>AI Analysis:</h4>
          <p>{analysis}</p>
        </div>
      )}
    </div>
  );
}

export default BehaviorAnalysis;
```

---

## 📱 6. Vue.js Example

```vue
<!-- CameraView.vue -->
<template>
  <div class="camera-view">
    <div class="video-container" ref="container">
      <img 
        :src="streamUrl" 
        @load="onImageLoad"
        ref="videoImg"
      />
      <canvas ref="canvas"></canvas>
    </div>
    
    <div class="detections-panel">
      <h3>Detections: {{ detections.length }}</h3>
      <div v-for="(det, idx) in detections" :key="idx" class="detection-item">
        <span class="label">{{ det.label }}</span>
        <span class="confidence">{{ (det.confidence * 100).toFixed(0) }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default {
  name: 'CameraView',
  props: {
    cameraId: Number,
    token: String
  },
  data() {
    return {
      stompClient: null,
      detections: [],
      streamUrl: '',
      videoSize: { width: 0, height: 0 }
    };
  },
  mounted() {
    this.streamUrl = `http://localhost:8081/api/v1/video/camera/${this.cameraId}/stream`;
    this.connectWebSocket();
  },
  methods: {
    connectWebSocket() {
      const socket = new SockJS('http://localhost:8081/ws');
      
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${this.token}`
        },
        onConnect: () => {
          this.stompClient.subscribe(
            `/topic/camera/${this.cameraId}/stream`,
            this.handleDetectionUpdate
          );
        }
      });
      
      this.stompClient.activate();
    },
    
    handleDetectionUpdate(message) {
      const data = JSON.parse(message.body);
      this.detections = data.detections || [];
      this.drawBoundingBoxes();
    },
    
    onImageLoad() {
      const img = this.$refs.videoImg;
      this.videoSize = {
        width: img.offsetWidth,
        height: img.offsetHeight
      };
    },
    
    drawBoundingBoxes() {
      const canvas = this.$refs.canvas;
      const ctx = canvas.getContext('2d');
      
      canvas.width = this.videoSize.width;
      canvas.height = this.videoSize.height;
      
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      
      this.detections.forEach(det => {
        const bbox = det.boundingBox;
        if (!bbox) return;
        
        ctx.strokeStyle = det.objectType === 'PERSON' ? '#00FF00' : '#0000FF';
        ctx.lineWidth = 3;
        ctx.strokeRect(bbox.x, bbox.y, bbox.width, bbox.height);
        
        ctx.fillStyle = ctx.strokeStyle;
        ctx.fillRect(bbox.x, bbox.y - 25, 150, 25);
        
        ctx.fillStyle = '#000';
        ctx.font = '16px Arial';
        ctx.fillText(
          `${det.label} ${(det.confidence * 100).toFixed(0)}%`,
          bbox.x + 5,
          bbox.y - 7
        );
      });
    }
  },
  beforeUnmount() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
};
</script>

<style scoped>
.camera-view {
  position: relative;
}

.video-container {
  position: relative;
  display: inline-block;
}

canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}

.detections-panel {
  margin-top: 10px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 5px;
}

.detection-item {
  display: flex;
  justify-content: space-between;
  padding: 5px;
  border-bottom: 1px solid #ddd;
}
</style>
```

---

## 🔐 7. Authentication

```javascript
// auth.js
import axios from 'axios';

const API_URL = 'http://localhost:8081/api';

export const login = async (username, password) => {
  const response = await axios.post(`${API_URL}/auth/login`, {
    username,
    password
  });
  
  const { token } = response.data;
  localStorage.setItem('token', token);
  
  return token;
};

export const getToken = () => {
  return localStorage.getItem('token');
};

// Setup axios interceptor for all requests
axios.interceptors.request.use(config => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

## 📦 8. Complete Package.json

```json
{
  "name": "camera-surveillance-frontend",
  "version": "1.0.0",
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "axios": "^1.6.0",
    "sockjs-client": "^1.6.1",
    "@stomp/stompjs": "^7.0.0",
    "react-router-dom": "^6.20.0"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.2.0",
    "vite": "^5.0.0"
  }
}
```

---

## 🚀 9. Quick Start

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 2. Add Camera
```bash
curl -X POST http://localhost:8081/api/cameras \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Camera Test",
    "code": "CAM-001",
    "rtspUrl": "rtsp://admin:pass@192.168.1.100:554/stream1",
    "status": "ACTIVE"
  }'
```

### 3. Start Detection
```bash
curl -X POST http://localhost:8081/api/stream-processing/camera/1/start-detection
```

### 4. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

### 5. Open Browser
```
http://localhost:5173
```

---

## 📊 Data Flow Summary

```
Camera (RTSP)
    ↓
Backend (Java)
    ├─ OpenCV: Capture frames
    ├─ YOLOv8: Detect objects
    ├─ OpenAI: Analyze behavior (optional)
    └─ WebSocket: Send to Frontend
        ↓
Frontend (React/Vue)
    ├─ Display video stream (MJPEG/Snapshot)
    ├─ Draw bounding boxes (Canvas)
    ├─ Show detection info
    └─ Display AI analysis
```

---

## 🎯 Best Practices

1. **Video Streaming:**
   - Use MJPEG for simplicity
   - Fall back to snapshot polling if needed
   - Consider WebRTC for better quality (advanced)

2. **WebSocket:**
   - Reconnect on disconnect
   - Handle errors gracefully
   - Unsubscribe when component unmounts

3. **Performance:**
   - Limit canvas redraws
   - Use React.memo for components
   - Debounce detection updates if needed

4. **Security:**
   - Always use HTTPS in production
   - Validate JWT token
   - Handle unauthorized access

5. **OpenAI:**
   - Only use for important events
   - Cache analysis results
   - Show cost estimates to users

---

## 🔍 Troubleshooting

1. **Video not loading:**
   - Check RTSP stream is active
   - Verify CORS settings
   - Try snapshot endpoint first

2. **WebSocket not connecting:**
   - Check token is valid
   - Verify backend WebSocket endpoint
   - Check browser console for errors

3. **No detections showing:**
   - Ensure detection is started
   - Check WebSocket subscription
   - Verify camera ID is correct

---

## 📚 Resources

- [STOMP Documentation](https://stomp-js.github.io/stomp-websocket/)
- [Canvas API](https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API)
- [OpenAI API](https://platform.openai.com/docs)

---

**🎉 Frontend và Backend giờ đã connect hoàn chỉnh!**
