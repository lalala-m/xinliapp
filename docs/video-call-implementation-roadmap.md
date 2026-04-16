# 视频通话功能开发路线图

## 📊 当前状态分析

### ✅ 已完成的部分
| 组件 | 状态 | 位置 |
|------|------|------|
| VideoCallActivity | ✅ 完成 | app/.../VideoCallActivity.java |
| OpenIMService (信令) | ✅ 完成 | app/.../openim/OpenIMService.java |
| LiveKit服务器端 | ✅ 完成 | Server/LiveKitController.java |
| WebRTC信令控制器 | ✅ 完成 | Server/WebRTCSignalingController.java |
| 聊天页面入口 | ✅ 完成 | assets/chat.html |
| Android权限 | ✅ 完成 | AndroidManifest.xml |

### ❌ 缺失的关键组件
| 组件 | 状态 | 说明 |
|------|------|------|
| LiveKitSessionManager | ❌ **缺失** | 最重要的媒体层封装类 |
| Gradle同步 | ❌ 未完成 | 需要同步项目依赖 |

---

## 🎯 第一阶段：修复基础问题（优先级：最高）

### 1.1 同步Gradle项目
```
在Android Studio中执行：
File → Sync Project with Gradle Files
或运行: TongYangYuan/sync_gradle.bat
```

### 1.2 验证依赖完整性
检查以下依赖是否正确加载：
- ✅ androidx.fragment:fragment:1.8.5
- ✅ androidx.annotation:annotation:1.8.2
- ✅ androidx.core:core:1.13.1
- ✅ io.livekit:livekit-android:2.6.0
- ✅ io.openim:android-sdk:3.8.3.2

---

## 🎯 第二阶段：创建缺失的核心组件（优先级：高）

### 2.1 创建 LiveKitSessionManager.java
**文件位置**: `app/src/main/java/com/example/tongyangyuan/LiveKitSessionManager.java`

这是最关键的缺失组件，负责：
- 连接LiveKit服务器
- 管理本地/远程视频轨道
- 处理音频/视频开关
- 管理通话状态

### 2.2 创建布局文件
**文件位置**: `app/src/main/res/layout/activity_video_call.xml`

需要包含：
- `remote_video_container` - 远程视频容器
- `local_video_container` - 本地视频容器
- `status_text` - 状态文字
- `btn_mute` - 静音按钮
- `btn_video` - 视频开关按钮
- `btn_hangup` - 挂断按钮
- `btn_back` - 返回按钮
- 振铃动画相关View

---

## 🎯 第三阶段：配置LiveKit服务器（优先级：高）

### 3.1 本地开发环境
```bash
# 确保Docker已启动
docker ps

# 启动LiveKit服务器
cd TongYangYuan-Server
docker-compose up -d livekit
```

### 3.2 验证LiveKit服务
```bash
# 访问LiveKit管理界面
http://localhost:7880
```

### 3.3 配置application.properties
```properties
# LiveKit配置（已配置）
livekit.url=ws://127.0.0.1:7880
livekit.api-key=devkey
livekit.api-secret=devsecret
```

---

## 🎯 第四阶段：端到端测试（优先级：中）

### 4.1 测试流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   家长端    │────▶│   服务器     │────▶│  咨询师端   │
│  (Android)  │     │  (Spring)   │     │   (Web)     │
└─────────────┘     └─────────────┘     └─────────────┘
      │                   │                   │
      │  1.发起通话请求    │                   │
      │──────────────────▶│                   │
      │                   │  2.转发CALL信令    │
      │                   │──────────────────▶│
      │                   │                   │
      │                   │  3.接收来电弹窗    │
      │                   │◀──────────────────│
      │                   │                   │
      │                   │  4.用户点击接听    │
      │                   │◀──────────────────│
      │                   │  5.转发ACCEPT信令  │
      │◀──────────────────│                   │
      │                   │                   │
      │  6.获取LiveKit    │                   │
      │     Token         │                   │
      │◀──────────────────│                   │
      │                   │                   │
      │  7.连接LiveKit    │                   │
      │     Room          │                   │
      │═══════════════════════════════════════│
      │       8.建立视频通话连接               │
      └───────────────────────────────────────┘
```

### 4.2 测试检查清单
- [ ] 家长端可以发起视频通话
- [ ] 咨询师端收到来电弹窗
- [ ] 咨询师端可以接听/拒绝
- [ ] 双方建立视频连接
- [ ] 通话结束消息正确显示

---

## 🎯 第五阶段：排查常见问题

### 问题1：LiveKit连接失败
**症状**: "音视频服务未配置"
**排查步骤**:
1. 检查Docker中LiveKit容器是否运行
2. 检查application.properties中livekit.url是否正确
3. 检查防火墙是否阻止了7880端口
4. 使用adb reverse映射端口（如需要）

### 问题2：信令传输失败
**症状**: 对方收不到来电
**排查步骤**:
1. 检查OpenIM容器是否运行
2. 检查WebSocket连接状态
3. 查看服务器日志中的STOMP消息
4. 验证appointmentId是否有效

### 问题3：视频画面黑屏
**症状**: 连接成功但无画面
**排查步骤**:
1. 检查摄像头权限
2. 检查Camera2 API兼容性
3. 验证WebView配置支持摄像头
4. 检查设备是否为模拟器（模拟器不支持）

### 问题4：音频无法播放
**症状**: 听不到对方声音
**排查步骤**:
1. 检查麦克风权限
2. 检查AudioManager配置
3. 验证扬声器是否开启
4. 检查系统静音模式

---

## 📝 LiveKitSessionManager 核心代码模板

```java
package com.example.tongyangyuan;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import livekit.io.livekit.android.engine.*
import livekit.io.livekit.android.room.*
import livekit.io.livekit.android.room.track.*
import java.util.*

public class LiveKitSessionManager {
    private static final String TAG = "LiveKitSession";
    
    private Room room;
    private LocalParticipant localParticipant;
    private Context context;
    
    public interface Callbacks {
        void onConnected();
        void onRoomDisconnected();
        void onRemoteParticipantLeft();
        void onRemoteVideoReady();
        void onError(String message);
    }
    
    private Callbacks callbacks;
    
    public LiveKitSessionManager(Context context, Callbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
    }
    
    public void connect(String serverUrl, String token, boolean isAudioOnly,
                       FrameLayout remoteContainer, FrameLayout localContainer,
                       Callbacks callbacks) {
        // 实现连接逻辑
    }
    
    public void disconnect(boolean sendEndSignal) {
        // 实现断开逻辑
    }
    
    public void setMicrophoneEnabled(boolean enabled) {
        // 实现麦克风控制
    }
    
    public void setCameraEnabled(boolean enabled) {
        // 实现摄像头控制
    }
}
```

---

## 🚀 快速启动命令

```bash
# 1. 同步Android项目
cd TongYangYuan
gradlew sync

# 2. 启动后端服务器
cd ../TongYangYuan-Server
mvn spring-boot:run

# 3. 启动LiveKit服务器
docker-compose up -d livekit

# 4. 端口映射（如使用模拟器）
adb reverse tcp:7880 tcp:7880
adb reverse tcp:8080 tcp:8080
```

---

## 📌 关键提醒

1. **appointmentId必须有效**: 通话发起时需要有效的预约ID
2. **用户必须已登录**: 需要先完成登录流程获取token
3. **预约状态必须为ACCEPTED**: 只有已确认的预约才能发起通话
4. **两端都需要在线**: 通话双方需要保持应用在前台或后台运行
5. **注意网络环境**: 开发时使用localhost，生产环境需要公网IP

---

## ✅ 验收标准

完成所有阶段后，系统应满足：
- [ ] Android端可以成功发起视频/语音通话
- [ ] Web端可以接收并处理来电
- [ ] 双方可以建立稳定的音视频连接
- [ ] 通话结束消息正确同步到聊天记录
- [ ] 通话时长正确计算和显示
