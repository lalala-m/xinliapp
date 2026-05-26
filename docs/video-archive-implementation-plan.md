# 视频存档功能实现方案

## 需求确认
- ✅ 通话开始即录制
- ✅ 通知双方正在录制
- ✅ 视频最大100MB，压缩优先保持内容
- ✅ 永久存储
- ✅ 咨询记录页面可查看视频

## 实现计划

### 1. Android端 (WebRTCVideoCallActivity.java)
- 添加录制状态UI（录制中红点 + "正在录制"文字）
- 通话连接成功后自动开始录制
- 使用 MediaRecorder 录制本地+远程视频混合流
- 通话结束时停止录制、压缩、上传
- 发送 VIDEO 类型消息到聊天记录

### 2. Web端 (webrtc-video-call.html)
- 添加录制状态UI
- 使用 MediaRecorder API 录制
- 通话结束上传视频

### 3. 服务器端（已支持）
- `/upload/video` 接口已存在
- 视频文件存储到 `uploads/videos/` 目录
- `chat_messages.media_url` 存储视频URL

### 4. 咨询记录详情页面
- 添加"视频记录"卡片
- 显示录制时间和播放链接

## 压缩策略
- 分辨率：480x360（优先内容可读性）
- 码率：256kbps（保持内容清晰）
- 编码：H.264
- 预计30分钟通话压缩后约50-70MB

## 数据流程
```
通话开始 → 显示"正在录制" → 开始MediaRecorder
         ↓
通话结束 → 停止录制 → 压缩 → 上传到服务器 → 获取media_url
         ↓
发送消息: messageType=VIDEO, media_url=xxx, content="视频通话录制"
         ↓
保存到chat_messages表
         ↓
咨询记录详情页面显示视频播放链接