# 通话信令无法到达咨询师端 - 问题排查与修复路线图

## 一、问题现象

**用户反馈**: 手机端（家长端）向咨询师发起通话请求，咨询师端（Web）完全没有收到来电通知。

## 二、系统架构分析

### 2.1 当前信令流程图

```
┌─────────────────┐                    ┌─────────────────┐                    ┌─────────────────┐
│   Android端      │                    │   Spring Boot   │                    │   Web端         │
│   (家长端)        │                    │   后端服务       │                    │   (咨询师端)     │
└────────┬────────┘                    └────────┬────────┘                    └────────┬────────┘
         │                                      │                                      │
         │  1. HTTP POST /messages               │                                      │
         │  (SYSTEM: CALL:call:video:xxx)        │                                      │
         │ ─────────────────────────────────────>│                                      │
         │                                      │                                      │
         │  2. 保存消息到数据库                   │                                      │
         │  3. 检测 CALL: 类型                   │                                      │
         │  4. STOMP → /user/{toUserId}/queue/webrtc                                   │
         │ ─────────────────────────────────────>│                                      │
         │                                      │                                      │
         │  同时 1b. STOMP ws:// → /app/webrtc.call                                     │
         │ ─────────────────────────────────────>│                                      │
         │                                      │                                      │
         │                                      │  SUBSCRIBE /user/queue/webrtc       │
         │                                      │<───────────────────────────────────── │
         │                                      │                                      │
         │                                      │  MESSAGE /user/{toUserId}/queue/webrtc
         │                                      │─────────────────────────────────────>│
         │                                      │                                      │
```

### 2.2 关键代码位置

| 组件 | 文件路径 | 关键方法 |
|------|---------|---------|
| Android发送 | `TongYangYuan/app/.../openim/OpenIMService.java` | `startCall()`, `doStartCall()` |
| 后端HTTP入口 | `TongYangYuan-Server/.../controller/ChatMessageController.java` | `sendMessage()`, `forwardCallSignalingToWebrtcIfNeeded()` |
| 后端WS入口 | `TongYangYuan-Server/.../websocket/WebRTCSignalingController.java` | `initiateCall()` |
| Web订阅 | `TongYangYuan-Web/js/chat.js` | `connectWebSocket()`, `handleWebRTCSignal()` |

---

## 三、问题根因分析

### 3.1 发现的潜在问题

#### 问题1: appointmentId 为空或无效 ⚠️ 关键问题

**文件**: `VideoCallActivity.java` 第210-213行

```java
private void startOutgoingCall() {
    // ...
    long apt = (appointmentId != null && appointmentId > 0) ? appointmentId : -1L;
    currentSessionId = openIMService.startCall(targetAccountId, callType, apt);
```

**问题**: 如果 `appointmentId` 为 null 或 <= 0，`startCall` 会跳过信令发送！

**证据**: `OpenIMService.java` 第551-558行:
```java
private void doStartCall(...) {
    if (appointmentId <= 0) {
        Log.w(TAG, "startCall: invalid appointmentId, skip signaling (咨询师端将收不到来电提示)");
        return;  // <-- 直接返回，不发送任何信令！
    }
```

#### 问题2: appointmentId 验证不一致

**Web端** `chat.js` 第275-277行:
```javascript
if (signal.appointmentId && String(signal.appointmentId) !== String(this.appointmentId)) {
    return;  // <-- 如果 appointmentId 不匹配，丢弃消息
}
```

**后端** `ChatMessageController.java` 正确设置了 `signal.setAppointmentId(saved.getAppointmentId())`

#### 问题3: Android端用户ID类型不匹配

- Android端使用 `Long` 类型存储 userId
- Web端使用 `String` 类型
- 后端 `convertAndSendToUser()` 使用 `String.valueOf(userId)`

### 3.2 排查清单

请按以下顺序排查（建议在Android Studio的Logcat中设置过滤器 `tag:OpenIMService` 或 `tag:VideoCallActivity`）:

| # | 检查项 | 预期值 | 如果不符合 |
|---|-------|-------|----------|
| 1 | Logcat中是否打印 "startCall: invalid appointmentId" | 不应出现 | 检查传入的 appointmentId |
| 2 | Logcat中是否打印 "OpenIM login OK" | 应该出现 | 检查OpenIM/JWT配置 |
| 3 | Logcat中是否打印 "WS recv" 包含 "call" | 应该出现 | 检查WebSocket连接 |
| 4 | 后端控制台是否打印 "收到WebRTC信令" | 应该出现 | 检查后端日志 |
| 5 | Web浏览器Console是否收到WebSocket消息 | 应该出现 | 检查网络和认证 |

---

## 四、修复方案

### 方案A: 修复 appointmentId 传递问题（推荐）

#### 步骤1: 确认传入参数

在 `WebAppInterface.java` 中添加日志:

```java
// WebAppInterface.java - 发起通话方法中添加
@JavascriptInterface
public void startVideoCall(String consultantId, String consultantName) {
    Log.d(TAG, "startVideoCall called: consultantId=" + consultantId + ", consultantName=" + consultantName);
    
    // 确保 appointmentId 正确传递
    Long aptId = getCurrentAppointmentId();  // 需要确保这个方法返回有效值
    if (aptId == null || aptId <= 0) {
        Log.e(TAG, "startVideoCall: appointmentId is invalid!");
        // 这里应该弹出提示或使用默认预约
    }
    
    // 调用原生通话
    Intent intent = new Intent(context, VideoCallActivity.class);
    // ...
}
```

#### 步骤2: 在 VideoCallActivity 中验证

```java
// VideoCallActivity.java - extractIntentData()
private void extractIntentData() {
    // ...
    appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, -1L);
    
    // 添加调试日志
    Log.d(TAG, "extractIntentData: appointmentId=" + appointmentId 
            + ", currentUserId=" + currentUserId 
            + ", targetUserId=" + targetUserId);
    
    if (appointmentId == -1L) {
        // 尝试从其他方式获取有效的 appointmentId
        Log.w(TAG, "appointmentId is -1, attempting to find valid appointment...");
        // 可以尝试从聊天会话中获取最近的预约ID
    }
}
```

### 方案B: 确保信令双通道发送

#### 步骤1: 修复 OpenIMService.doStartCall()

```java
// OpenIMService.java - doStartCall() 方法
private void doStartCall(String targetAccountId, String callType, long appointmentId, String sessionId, long senderId) {
    // 移除 appointmentId <= 0 的提前返回
    // 即使没有有效预约，也应该发送信令（用于紧急通话场景）
    
    // 1. 优先通过 HTTP POST 发送（会被后端转发到 Web）
    sendCallSignalViaHttp(targetAccountId, callType, appointmentId, sessionId, senderId);
    
    // 2. 同时通过 WebSocket STOMP 发送（直接通道）
    sendCallSignalViaWebSocket(targetAccountId, callType, appointmentId, sessionId);
}
```

---

## 五、实施计划（按优先级）

### Phase 1: 添加诊断日志（30分钟）

**目标**: 定位问题发生在哪个环节

1. 在 `VideoCallActivity.startOutgoingCall()` 添加完整参数日志
2. 在 `OpenIMService.startCall()` 和 `doStartCall()` 添加日志
3. 在 `ChatMessageController.sendMessage()` 添加日志
4. 在 `WebRTCSignalingController.initiateCall()` 添加日志

**验证方法**: 
- 启动Android应用，清除Logcat
- 发起通话请求
- 检查日志输出，确定在哪一步失败

### Phase 2: 修复 appointmentId 问题（1小时）

**目标**: 确保预约ID正确传递

1. 检查 `WebAppInterface.startVideoCall()` 的调用来源和参数
2. 确保预约ID在HTML/JavaScript层面正确获取
3. 在VideoCallActivity中添加fallback逻辑

### Phase 3: 修复信令双通道（1小时）

**目标**: 确保Android端的信令能到达Web端

1. 修改 `OpenIMService.java`，移除 appointmentId=0 时的提前返回
2. 确保HTTP和WebSocket两个通道都能正确发送
3. 在后端添加日志，确认收到请求

### Phase 4: 端到端测试（30分钟）

**目标**: 验证修复有效

1. 重启后端服务
2. Android端重新安装
3. 发起通话测试
4. 检查咨询师端Web界面是否显示来电通知

---

## 六、测试用例

### 测试1: 正常预约场景

**前提条件**: 存在有效的预约记录

**操作步骤**:
1. 家长在APP中打开与咨询师的聊天页面
2. 点击视频通话按钮
3. 观察咨询师端Web界面

**预期结果**: 
- Android端显示"等待接听"
- Web端显示来电弹窗

### 测试2: appointmentId 无效场景

**操作步骤**:
1. 手动修改代码，使 appointmentId = -1
2. 发起通话请求
3. 检查日志输出

**预期结果**: 
- 日志应显示 "startCall: invalid appointmentId"
- 这是当前的问题点

---

## 七、关键代码修改汇总

### 7.1 VideoCallActivity.java

需要修改 `extractIntentData()` 方法:

```java
private void extractIntentData() {
    Intent intent = getIntent();
    if (intent != null) {
        appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, -1L);
        currentUserId = intent.getLongExtra(EXTRA_CURRENT_USER_ID, -1L);
        targetUserId = intent.getLongExtra(EXTRA_TARGET_USER_ID, -1L);
        callType = intent.getStringExtra(KEY_CALL_TYPE);
        consultantName = intent.getStringExtra(KEY_CONSULTANT_NAME);
        isCaller = intent.getBooleanExtra(KEY_IS_CALLER, false);
        isAudioCall = "audio".equalsIgnoreCase(callType);
    }

    // 诊断日志
    Log.d(TAG, "extractIntentData: appointmentId=" + appointmentId 
            + ", currentUserId=" + currentUserId 
            + ", targetUserId=" + targetUserId 
            + ", callType=" + callType
            + ", isCaller=" + isCaller);

    if (currentUserId == -1L || targetUserId == -1L) {
        Toast.makeText(this, "通话数据无效", Toast.LENGTH_SHORT).show();
        finish();
        return;
    }

    if (consultantName == null) {
        consultantName = "咨询师";
    }
    
    // 如果 appointmentId 无效，生成一个临时ID用于信令传递
    if (appointmentId == null || appointmentId <= 0) {
        // 创建一个基于时间戳的临时ID
        appointmentId = System.currentTimeMillis() % 100000;
        Log.w(TAG, "Using temporary appointmentId: " + appointmentId);
    }
}
```

### 7.2 OpenIMService.java

需要修改 `doStartCall()` 方法，移除提前返回:

```java
private void doStartCall(String targetAccountId, String callType, long appointmentId, String sessionId, long senderId) {
    // 移除这段代码:
    // if (appointmentId <= 0) {
    //     Log.w(TAG, "startCall: invalid appointmentId, skip signaling...");
    //     return;
    // }
    
    // 即使 appointmentId 无效，也应该发送信令（用于紧急/测试场景）
    Log.d(TAG, "doStartCall: target=" + targetAccountId + ", type=" + callType 
            + ", appointmentId=" + appointmentId + ", sessionId=" + sessionId);
    
    // 继续发送信令...
}
```

---

## 八、验证清单

修复完成后，请按以下顺序验证:

- [ ] 1. Android Logcat 中没有 "invalid appointmentId" 警告
- [ ] 2. Android Logcat 显示 "OpenIM login OK"
- [ ] 3. Android Logcat 显示 "WS recv" 包含 "call" 类型消息
- [ ] 4. 后端控制台显示 "收到WebRTC信令: type=call"
- [ ] 5. 后端控制台显示 "视频通话请求已发送"
- [ ] 6. Web浏览器Console显示收到 WebSocket 消息
- [ ] 7. Web界面显示来电通知弹窗

---

## 九、联系信息

如果以上排查步骤无法解决问题，请提供:

1. Android Logcat 的完整输出（过滤 `OpenIMService` 或 `VideoCallActivity`）
2. 后端服务的启动日志
3. Web浏览器的Console输出（F12 → Console）
4. 通话发起时使用的预约ID（可以从URL或日志中获取）
