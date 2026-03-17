# Hướng Dẫn Tải YOLOv8 Model

## 🎯 Mục đích
Tải file `yolov8n.onnx` để sử dụng AI detection trong hệ thống Camera Surveillance.

---

## ✅ Cách 1: Dùng Python (Khuyên dùng - Nhanh nhất)

### Bước 1: Cài Python
- Download Python từ: https://www.python.org/downloads/
- Tick ✅ "Add Python to PATH" khi cài

### Bước 2: Mở PowerShell/CMD và chạy:

```powershell
# Cài Ultralytics
pip install ultralytics

# Export model sang ONNX
yolo export model=yolov8n.pt format=onnx
```

**📝 LƯU Ý:** Nếu gặp lỗi IR version:
```powershell
# Export với opset 11 (tương thích hơn)
yolo export model=yolov8n.pt format=onnx opset=11
```

### Bước 3: Copy file vào project

```powershell
# File yolov8n.onnx sẽ ở folder hiện tại
# Copy vào project models folder
move yolov8n.onnx "D:\Camera Surveillance System_one\Camera _Surveillance _System\models\yolov8n.onnx"
```

**✅ Xong! Restart IntelliJ để load model.**

**📝 LƯU Ý:** Project đã chuyển sang dùng **ONNX Runtime** thay vì OpenCV DNN, nên export model bình thường (không cần opset=12).

**✅ Xong! Restart IntelliJ để load model.**

---

## 📥 Cách 2: Download trực tiếp (Nếu không có Python)

### Option A: Google Drive (Nhanh)
1. Download từ: https://drive.google.com/file/d/1-yG0s8Y_qT3MQ8PtF0FQ7qQ8lKj8zQlJ
2. Save vào: `D:\Camera Surveillance System_one\Camera _Surveillance _System\models\yolov8n.onnx`

### Option B: GitHub Releases
1. Vào: https://github.com/ultralytics/assets/releases
2. Tìm release có file `yolov8n.onnx`
3. Click Download
4. Save vào folder `models`

---

## 🔍 Kiểm tra

### File structure phải như này:
```
Camera _Surveillance _System/
├── models/
│   └── yolov8n.onnx  ← File này (khoảng 6MB)
├── src/
├── pom.xml
└── ...
```

### Trong IntelliJ:
1. Restart application
2. Kiểm tra console log:
   - ✅ **Thành công:** `✅ YOLOv8 model loaded successfully`
   - ❌ **Lỗi:** `⚠️ YOLOv8 model not loaded`

---

## ⚙️ Model sizes (Tuỳ chọn)

| Model | Size | Speed | Accuracy |
|-------|------|-------|----------|
| yolov8n.onnx | 6MB | ⚡⚡⚡ Rất nhanh | ⭐⭐ Tốt |
| yolov8s.onnx | 22MB | ⚡⚡ Nhanh | ⭐⭐⭐ Rất tốt |
| yolov8m.onnx | 52MB | ⚡ Trung bình | ⭐⭐⭐⭐ Xuất sắc |

**Khuyên dùng:** `yolov8n.onnx` - Đủ nhanh và chính xác cho camera surveillance

---

## 🆘 Troubleshooting

### Lỗi: "pip not recognized"
→ Chưa cài Python hoặc chưa add to PATH
→ Cài lại Python, nhớ tick "Add Python to PATH"

### Lỗi: "yolo command not found"
```bash
# Thử cách này thay thế:
python -m ultralytics export model=yolov8n.pt format=onnx
```

### Lỗi: "Model not loaded" sau khi copy file
→ Kiểm tra đường dẫn chính xác:
```
D:\Camera Surveillance System_one\Camera _Surveillance _System\models\yolov8n.onnx
```
→ Restart IntelliJ

### Không có Python và không muốn cài?
→ Hỏi người có Python export giúp, hoặc download từ Google Drive (Option 2A)

---

## 📞 Liên hệ hỗ trợ
Nếu vẫn gặp vấn đề, check application.properties:
```properties
ai.yolo.model.path=models/yolov8n.onnx
```

Đường dẫn tương đối từ root project.
