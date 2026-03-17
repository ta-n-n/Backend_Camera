# 🔧 Frontend Error Fix Guide

## ❌ Lỗi Gặp Phải

### 1. HLS Stream Not Found (404)
```
GET http://localhost:8888/camera1/index.m3u8 404 (Not Found)
```

### 2. Backend API Error (500)
```
GET http://localhost:8081/api/v1/stream/status/1 500 (Internal Server Error)
```

---

## 🔍 Nguyên Nhân

### Lỗi 1: HLS Stream 404

**Backend KHÔNG serve HLS stream!**

Backend hiện tại chỉ:
- ✅ Đọc RTSP stream nội bộ (OpenCV VideoCapture)
- ✅ Chạy AI detection (YOLOv8)
- ✅ Lưu kết quả vào database
- ✅ Push AI events qua WebSocket

Backend **KHÔNG có**:
- ❌ HLS server để serve `.m3u8` files
- ❌ HTTP endpoint để stream video
- ❌ FFmpeg pipeline để convert RTSP → HLS

### Lỗi 2: Backend 500 Error

Có thể do:
1. **StreamMonitorService chưa được start**
2. Camera không tồn tại trong database
3. NullPointerException trong service
4. Database connection issue

---

## ✅ Giải Pháp

### **Option 1: Dùng WebSocket để Nhận Snapshots** ⭐ Recommended

Backend push snapshots (Base64 images) qua WebSocket thay vì stream HLS.

#### Backend: Thêm method push snapshots

```java
// VideoStreamServiceImpl.java - Thêm method này
public void startSnapshotPush(Long cameraId, int fps) {
    if (!isStreamRunning(cameraId)) {
        return;
    }
    
    // Schedule task push snapshot mỗi 1000/fps ms
    scheduledExecutor.scheduleAtFixedRate(() -> {
        try {
            Mat frame = getCurrentFrame(cameraId);
            if (frame != null && !frame.empty()) {
                // Encode to JPEG
                MatOfByte mob = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, mob);
                byte[] imageBytes = mob.toArray();
                
                // Convert to Base64
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                
                // Push qua WebSocket
                Map<String, Object> data = Map.of(
                    "cameraId", cameraId,
                    "image", "data:image/jpeg;base64," + base64,
                    "timestamp", System.currentTimeMillis()
                );
                
                webSocketPublisher.publishCameraStreamUpdate(cameraId, data);
            }
        } catch (Exception e) {
            log.error("Error pushing snapshot for camera {}", cameraId, e);
        }
    }, 0, 1000 / fps, TimeUnit.MILLISECONDS);
}
```

#### Frontend: Nhận snapshots qua WebSocket

```tsx
// CameraLiveView.tsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function CameraLiveView({ cameraId }) {
  const [imageData, setImageData] = useState('');
  const [detections, setDetections] = useState([]);
  
  useEffect(() => {
    const socket = new SockJS('http://localhost:8081/ws');
    const client = Stomp.over(socket);
    
    client.connect({}, () => {
      // Subscribe video snapshots
      client.subscribe(`/topic/camera/${cameraId}/stream`, (msg) => {
        const data = JSON.parse(msg.body);
        setImageData(data.image); // Base64 image
      });
      
      // Subscribe AI detections
      client.subscribe(`/topic/camera/${cameraId}/ai-events`, (msg) => {
        const event = JSON.parse(msg.body);
        setDetections(event.objects);
      });
    });
    
    return () => client.disconnect();
  }, [cameraId]);
  
  return (
    <div style={{ position: 'relative' }}>
      {/* Display snapshot */}
      {imageData && (
        <img src={imageData} alt="Camera" style={{ width: '100%' }} />
      )}
      
      {/* Bounding boxes */}
      {detections.map((det, i) => (
        <div key={i} style={{
          position: 'absolute',
          left: det.boundingBox.x,
          top: det.boundingBox.y,
          width: det.boundingBox.width,
          height: det.boundingBox.height,
          border: '2px solid red'
        }}>
          {det.className} {(det.confidence * 100).toFixed(0)}%
        </div>
      ))}
    </div>
  );
}
```

---

### **Option 2: Setup HLS Server Riêng** (Nếu muốn video mượt)

#### Bước 1: Chạy FFmpeg để convert RTSP → HLS

```bash
# Windows
ffmpeg -i rtsp://localhost:8554/camera1 ^
  -c:v libx264 -preset ultrafast -tune zerolatency ^
  -f hls -hls_time 2 -hls_list_size 3 ^
  -hls_flags delete_segments+append_list ^
  D:\hls-streams\camera1\index.m3u8

# Linux/Mac
ffmpeg -i rtsp://localhost:8554/camera1 \
  -c:v libx264 -preset ultrafast -tune zerolatency \
  -f hls -hls_time 2 -hls_list_size 3 \
  -hls_flags delete_segments+append_list \
  /var/www/hls-streams/camera1/index.m3u8
```

#### Bước 2: Setup HTTP Server để serve HLS files

**Option A: Nginx**

```nginx
# nginx.conf
http {
    server {
        listen 8888;
        
        location /camera1 {
            alias D:/hls-streams/camera1;
            add_header Cache-Control no-cache;
            add_header Access-Control-Allow-Origin *;
            types {
                application/vnd.apple.mpegurl m3u8;
                video/mp2t ts;
            }
        }
        
        location /camera2 {
            alias D:/hls-streams/camera2;
            add_header Cache-Control no-cache;
            add_header Access-Control-Allow-Origin *;
        }
    }
}
```

**Option B: Python Simple HTTP Server**

```bash
cd D:\hls-streams
python -m http.server 8888 --bind 0.0.0.0
```

#### Bước 3: Frontend đọc HLS

Frontend code giữ nguyên, nhưng đảm bảo HLS server đã chạy:

```tsx
import Hls from 'hls.js';

function HLSPlayer({ streamUrl }) {
  const videoRef = useRef();
  
  useEffect(() => {
    if (Hls.isSupported()) {
      const hls = new Hls({
        enableWorker: true,
        lowLatencyMode: true,
        backBufferLength: 90
      });
      
      hls.loadSource(streamUrl); // http://localhost:8888/camera1/index.m3u8
      hls.attachMedia(videoRef.current);
      
      hls.on(Hls.Events.ERROR, (event, data) => {
        console.error('[HLS] Error:', data);
        if (data.fatal) {
          if (data.type === Hls.ErrorTypes.NETWORK_ERROR) {
            console.log('Network error, attempting recovery...');
            hls.startLoad();
          }
        }
      });
      
      return () => hls.destroy();
    }
  }, [streamUrl]);
  
  return <video ref={videoRef} controls autoPlay />;
}
```

---

### **Fix Backend 500 Error**

#### 1. Kiểm tra backend logs

Xem file log hoặc console để tìm stacktrace:

```
tail -f logs/spring.log
```

Hoặc check application logs trong IDE console.

#### 2. Đảm bảo camera tồn tại

```sql
-- Kiểm tra camera ID=1
SELECT * FROM cameras WHERE id = 1;
```

Nếu không có, tạo camera:

```http
POST http://localhost:8081/api/v1/cameras
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "name": "Camera 1",
  "code": "CAM001",
  "rtspUrl": "rtsp://localhost:8554/camera1",
  "status": "ACTIVE",
  "locationId": 1
}
```

#### 3. Fix NullPointerException trong StreamMonitorService

Thêm null checks vào `StreamProcessingController.java`:

```java
@GetMapping("/status/{cameraId}")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
public ResponseEntity<ApiResponse<StreamStatusResponse>> getStreamStatus(@PathVariable Long cameraId) {
    try {
        Camera camera = cameraRepository.findById(cameraId).orElse(null);
        if (camera == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<StreamStatusResponse>builder()
                    .success(false)
                    .message("Camera không tồn tại")
                    .build());
        }

        boolean isRunning = videoStreamService.isStreamRunning(cameraId);
        
        // Fix: Handle case when StreamMonitorService not started
        boolean isHealthy = false;
        try {
            isHealthy = streamMonitorService.isStreamHealthy(cameraId);
        } catch (Exception e) {
            log.warn("Could not get health status for camera {}: {}", cameraId, e.getMessage());
            isHealthy = isRunning; // Fallback: nếu đang chạy thì coi là healthy
        }

        StreamStatusResponse response = StreamStatusResponse.builder()
            .cameraId(cameraId)
            .cameraName(camera.getName())
            .rtspUrl(camera.getRtspUrl())
            .isRunning(isRunning)
            .isHealthy(isHealthy)
            .status(isRunning ? "RUNNING" : "STOPPED")
            .lastCheckTime(LocalDateTime.now())
            .build();

        return ResponseEntity.ok(ApiResponse.<StreamStatusResponse>builder()
            .success(true)
            .data(response)
            .build());
            
    } catch (Exception e) {
        log.error("Error getting stream status for camera {}", cameraId, e);
        return ResponseEntity.status(500)
            .body(ApiResponse.<StreamStatusResponse>builder()
                .success(false)
                .message("Lỗi server: " + e.getMessage())
                .build());
    }
}
```

#### 4. Thêm @ControllerAdvice global exception handler

```java
// exception/GlobalExceptionHandler.java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("Unhandled exception:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<String>builder()
                .success(false)
                .message("Internal server error: " + e.getMessage())
                .build());
    }
    
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<String>> handleNullPointer(NullPointerException e) {
        log.error("Null pointer exception:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<String>builder()
                .success(false)
                .message("Null pointer error - check server logs")
                .build());
    }
}
```

---

## 🎯 Checklist Fix Nhanh

### Backend

- [ ] Kiểm tra logs để tìm lỗi 500
- [ ] Đảm bảo camera ID tồn tại trong database
- [ ] Add try-catch vào API `/stream/status/{cameraId}`
- [ ] Thêm GlobalExceptionHandler
- [ ] (Optional) Implement snapshot push qua WebSocket

### Frontend

**Nếu dùng WebSocket Snapshots:**
- [ ] Remove HLS player code
- [ ] Implement WebSocket subscriber để nhận Base64 images
- [ ] Display images và bounding boxes

**Nếu dùng HLS:**
- [ ] Setup FFmpeg convert RTSP → HLS
- [ ] Chạy HTTP server (Nginx/Python) trên port 8888
- [ ] Đảm bảo CORS headers được set
- [ ] Test HLS stream: `curl http://localhost:8888/camera1/index.m3u8`

---

## 🚀 Flow Đúng Để Test

### 1. Setup RTSP Stream

```bash
# Chạy MediaMTX
docker run -d --name mediamtx -p 8554:8554 bluenviron/mediamtx

# Stream video qua FFmpeg
ffmpeg -re -stream_loop -1 -i D:\video.mp4 -c copy -f rtsp rtsp://localhost:8554/camera1
```

### 2. Tạo Camera trong DB

```http
POST http://localhost:8081/api/v1/cameras
{
  "name": "Camera 1",
  "code": "CAM001",
  "rtspUrl": "rtsp://localhost:8554/camera1",
  "status": "ACTIVE"
}
```

### 3. Start Backend AI Detection

```http
POST http://localhost:8081/api/v1/stream/detection/start/1
Authorization: Bearer YOUR_TOKEN
```

### 4. Frontend Connect WebSocket

```javascript
const socket = new SockJS('http://localhost:8081/ws');
const client = Stomp.over(socket);

client.connect({}, () => {
  // Subscribe AI events
  client.subscribe('/topic/camera/1/ai-events', (msg) => {
    console.log('AI Event:', JSON.parse(msg.body));
  });
});
```

### 5. (Optional) Setup HLS nếu muốn video

```bash
# Convert RTSP → HLS
ffmpeg -i rtsp://localhost:8554/camera1 -f hls -hls_time 2 D:\hls\camera1\index.m3u8

# Serve HLS
cd D:\hls
python -m http.server 8888
```

---

## 📞 Still Having Issues?

1. Check backend logs: `tail -f logs/application.log`
2. Check database: `SELECT * FROM cameras;`
3. Test RTSP: `ffplay rtsp://localhost:8554/camera1`
4. Test API: `curl http://localhost:8081/api/v1/stream/status/1`
5. Check WebSocket: Browser DevTools → Network → WS tab

---

## ✅ Tóm Tắt

| Lỗi | Nguyên nhân | Giải pháp |
|-----|-------------|-----------|
| **404 HLS** | Backend không serve HLS | Setup FFmpeg + HTTP server HOẶC dùng WebSocket snapshots |
| **500 Backend** | Exception trong API | Add try-catch, check null, handle errors gracefully |
| **No video** | Không có video source | Dùng WebSocket Base64 images hoặc setup HLS riêng |

**Khuyến nghị:** Dùng **WebSocket snapshots** (Option 1) vì đơn giản, không cần setup thêm services.
