from flask import Flask, request, jsonify
from flask_cors import CORS
from ultralytics import YOLO
import os
import base64
from PIL import Image
import io
import time

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# 加载 YOLOv8 模型 (使用最轻量级的 nano 版本)
# 首次运行时会自动下载 yolov8n.pt
model = YOLO('yolov8n.pt')
pose_model = YOLO('yolov8n-pose.pt')

@app.route('/predict', methods=['POST'])
def predict():
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image file provided'}), 400
        
        file = request.files['image']
        
        # 读取图片
        img_bytes = file.read()
        img = Image.open(io.BytesIO(img_bytes))
        
        # 进行预测
        results = model(img)
        
        # 处理结果
        detections = []
        for result in results:
            boxes = result.boxes
            for box in boxes:
                # 获取坐标 (x1, y1, x2, y2)
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                
                # 获取置信度和类别
                conf = float(box.conf[0])
                cls = int(box.cls[0])
                label = model.names[cls]
                
                detections.append({
                    'label': label,
                    'confidence': conf,
                    'bbox': [x1, y1, x2, y2]
                })
        
        return jsonify({
            'success': True,
            'detections': detections
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/predict_pose', methods=['POST'])
def predict_pose():
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image file provided'}), 400
        
        file = request.files['image']
        
        # 读取图片
        img_bytes = file.read()
        img = Image.open(io.BytesIO(img_bytes))
        
        # 进行预测 (Pose)
        results = pose_model(img)
        
        # 处理结果
        poses = []
        for result in results:
            # keypoints
            if result.keypoints is not None:
                # xy shape: (num_persons, 17, 2)
                # conf shape: (num_persons, 17)
                keypoints_xy = result.keypoints.xy.tolist()
                keypoints_conf = result.keypoints.conf.tolist() if result.keypoints.conf is not None else None
                
                # 同时也获取 bbox
                boxes = result.boxes.xyxy.tolist()
                
                for i in range(len(keypoints_xy)):
                    poses.append({
                        'keypoints': keypoints_xy[i],
                        'keypoints_conf': keypoints_conf[i] if keypoints_conf else [],
                        'bbox': boxes[i]
                    })
        
        return jsonify({
            'success': True,
            'poses': poses
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'model': 'yolov8n'})

if __name__ == '__main__':
    # 监听所有 IP，端口 5000
    app.run(host='0.0.0.0', port=5000, debug=False)
