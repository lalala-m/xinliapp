# 客户端集成指南

本文档详细说明如何在Android端和Web端集成服务器API和WebSocket功能。

## 目录
1. [Android端集成](#android端集成)
2. [Web端集成](#web端集成)
3. [API调用示例](#api调用示例)
4. [WebSocket集成](#websocket集成)
5. [WebRTC视频通话集成](#webrtc视频通话集成)

---

## Android端集成

### 1. 添加依赖

在 `app/build.gradle` 中添加以下依赖：

```gradle
dependencies {
    // 网络请求
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

    // WebSocket
    implementation 'org.java-websocket:Java-WebSocket:1.5.3'

    // STOMP协议
    implementation 'com.github.NaikSoftware:StompProtocolAndroid:1.6.6'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

    // WebRTC
    implementation 'org.webrtc:google-webrtc:1.0.32006'

    // JSON解析
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### 2. 网络权限配置

在 `AndroidManifest.xml` 中添加：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. 创建API接口

```java
// ApiService.java
public interface ApiService {
    String BASE_URL = "http://你的服务器IP:8080/api/";

    @POST("auth/login")
    Call<ApiResponse<LoginResult>> login(@Body LoginRequest request);

    @GET("consultants")
    Call<ApiResponse<List<Consultant>>> getConsultants(@Header("Authorization") String token);

    @GET("appointments/consultant/{consultantId}")
    Call<ApiResponse<List<Appointment>>> getAppointments(
        @Path("consultantId") Long consultantId,
        @Header("Authorization") String token
    );

    @POST("appointments")
    Call<ApiResponse<Appointment>> createAppointment(
        @Body Appointment appointment,
        @Header("Authorization") String token
    );

    @GET("messages/appointment/{appointmentId}")
    Call<ApiResponse<List<ChatMessage>>> getMessages(
        @Path("appointmentId") Long appointmentId,
        @Header("Authorization") String token
    );
}
```

### 4. 创建Retrofit客户端

```java
// RetrofitClient.java
public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
```

### 5. WebSocket连接（使用STOMP）

```java
// WebSocketManager.java
public class WebSocketManager {
    private StompClient stompClient;
    private String serverUrl = "ws://你的服务器IP:8080/api/ws";
    private Long userId;

    public void connect(Long userId) {
        this.userId = userId;

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl);

        stompClient.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(lifecycleEvent -> {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d("WebSocket", "连接成功");
                        subscribeToMessages();
                        notifyOnline();
                        break;
                    case CLOSED:
                        Log.d("WebSocket", "连接关闭");
                        break;
                    case ERROR:
                        Log.e("WebSocket", "连接错误", lifecycleEvent.getException());
                        break;
                }
            });

        stompClient.connect();
    }

    private void subscribeToMessages() {
        // 订阅聊天消息
        stompClient.topic("/user/queue/messages")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(topicMessage -> {
                String json = topicMessage.getPayload();
                ChatMessage message = new Gson().fromJson(json, ChatMessage.class);
                // 处理接收到的消息
                onMessageReceived(message);
            });

        // 订阅WebRTC信令
        stompClient.topic("/user/queue/webrtc")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(topicMessage -> {
                String json = topicMessage.getPayload();
                WebRTCSignal signal = new Gson().fromJson(json, WebRTCSignal.class);
                // 处理WebRTC信令
                onWebRTCSignalReceived(signal);
            });
    }

    private void notifyOnline() {
        stompClient.send("/app/chat.online", String.valueOf(userId))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe();
    }

    public void sendMessage(ChatMessageDTO message) {
        String json = new Gson().toJson(message);
        stompClient.send("/app/chat.send", json)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe();
    }

    public void sendWebRTCSignal(WebRTCSignal signal) {
        String json = new Gson().toJson(signal);
        stompClient.send("/app/webrtc.signal", json)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe();
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.send("/app/chat.offline", String.valueOf(userId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
            stompClient.disconnect();
        }
    }

    // 回调方法，需要在Activity中实现
    protected void onMessageReceived(ChatMessage message) {}
    protected void onWebRTCSignalReceived(WebRTCSignal signal) {}
}
```

### 6. 在Activity中使用

```java
// ChatActivity.java
public class ChatActivity extends AppCompatActivity {
    private WebSocketManager webSocketManager;
    private Long userId;
    private Long appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userId = getIntent().getLongExtra("userId", 0);
        appointmentId = getIntent().getLongExtra("appointmentId", 0);

        // 初始化WebSocket
        webSocketManager = new WebSocketManager() {
            @Override
            protected void onMessageReceived(ChatMessage message) {
                runOnUiThread(() -> {
                    // 更新UI显示新消息
                    addMessageToUI(message);
                });
            }
        };

        webSocketManager.connect(userId);

        // 加载历史消息
        loadHistoryMessages();
    }

    private void loadHistoryMessages() {
        String token = "Bearer " + getStoredToken();
        RetrofitClient.getApiService()
            .getMessages(appointmentId, token)
            .enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<ChatMessage>>> call,
                                     Response<ApiResponse<List<ChatMessage>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ChatMessage> messages = response.body().getData();
                        // 显示历史消息
                        displayMessages(messages);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) {
                    Log.e("ChatActivity", "加载消息失败", t);
                }
            });
    }

    private void sendMessage(String content) {
        ChatMessageDTO message = new ChatMessageDTO();
        message.setAppointmentId(appointmentId);
        message.setSenderUserId(userId);
        message.setReceiverUserId(receiverUserId);
        message.setMessageType("TEXT");
        message.setContent(content);
        message.setIsFromConsultant(isConsultant);

        webSocketManager.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}
```

---

## Web端集成

### 1. 引入依赖

在HTML中引入SockJS和STOMP库：

```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
```

### 2. API调用封装

```javascript
// api.js
const API_BASE_URL = 'http://你的服务器IP:8080/api';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    async login(phone, password) {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ phone, password })
        });
        const result = await response.json();
        if (result.code === 200) {
            this.token = result.data.token;
            localStorage.setItem('token', this.token);
        }
        return result;
    }

    async getConsultants() {
        const response = await fetch(`${API_BASE_URL}/consultants`, {
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });
        return await response.json();
    }

    async getAppointments(consultantId) {
        const response = await fetch(
            `${API_BASE_URL}/appointments/consultant/${consultantId}`,
            {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            }
        );
        return await response.json();
    }

    async getMessages(appointmentId) {
        const response = await fetch(
            `${API_BASE_URL}/messages/appointment/${appointmentId}`,
            {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            }
        );
        return await response.json();
    }

    async createAppointment(appointment) {
        const response = await fetch(`${API_BASE_URL}/appointments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.token}`
            },
            body: JSON.stringify(appointment)
        });
        return await response.json();
    }
}

const apiClient = new ApiClient();
```

### 3. WebSocket连接

```javascript
// websocket.js
class WebSocketClient {
    constructor() {
        this.stompClient = null;
        this.userId = null;
        this.connected = false;
    }

    connect(userId) {
        this.userId = userId;
        const socket = new SockJS('http://你的服务器IP:8080/api/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket连接成功:', frame);
            this.connected = true;

            // 订阅消息
            this.subscribeToMessages();

            // 通知服务器用户上线
            this.notifyOnline();

            // 触发连接成功回调
            if (this.onConnected) {
                this.onConnected();
            }
        }, (error) => {
            console.error('WebSocket连接失败:', error);
            this.connected = false;

            // 5秒后重连
            setTimeout(() => this.connect(userId), 5000);
        });
    }

    subscribeToMessages() {
        // 订阅聊天消息
        this.stompClient.subscribe('/user/queue/messages', (message) => {
            const chatMessage = JSON.parse(message.body);
            console.log('收到消息:', chatMessage);

            if (this.onMessageReceived) {
                this.onMessageReceived(chatMessage);
            }
        });

        // 订阅WebRTC信令
        this.stompClient.subscribe('/user/queue/webrtc', (message) => {
            const signal = JSON.parse(message.body);
            console.log('收到WebRTC信令:', signal);

            if (this.onWebRTCSignal) {
                this.onWebRTCSignal(signal);
            }
        });

        // 订阅在线状态
        this.stompClient.subscribe('/topic/online-status', (message) => {
            const status = JSON.parse(message.body);
            console.log('用户状态变化:', status);

            if (this.onUserStatusChanged) {
                this.onUserStatusChanged(status);
            }
        });

        // 订阅输入状态
        this.stompClient.subscribe('/user/queue/typing', (message) => {
            const typingUserId = JSON.parse(message.body);

            if (this.onUserTyping) {
                this.onUserTyping(typingUserId);
            }
        });
    }

    notifyOnline() {
        this.stompClient.send('/app/chat.online', {}, this.userId);
    }

    sendMessage(messageData) {
        if (this.connected) {
            this.stompClient.send('/app/chat.send', {}, JSON.stringify(messageData));
        } else {
            console.error('WebSocket未连接');
        }
    }

    sendTypingStatus(receiverUserId) {
        if (this.connected) {
            this.stompClient.send('/app/chat.typing', {}, JSON.stringify({
                senderUserId: this.userId,
                receiverUserId: receiverUserId
            }));
        }
    }

    sendWebRTCSignal(signal) {
        if (this.connected) {
            this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(signal));
        }
    }

    initiateCall(toUserId, appointmentId) {
        if (this.connected) {
            this.stompClient.send('/app/webrtc.call', {}, JSON.stringify({
                fromUserId: this.userId,
                toUserId: toUserId,
                appointmentId: appointmentId
            }));
        }
    }

    acceptCall(toUserId, appointmentId) {
        if (this.connected) {
            this.stompClient.send('/app/webrtc.accept', {}, JSON.stringify({
                fromUserId: this.userId,
                toUserId: toUserId,
                appointmentId: appointmentId
            }));
        }
    }

    rejectCall(toUserId, appointmentId) {
        if (this.connected) {
            this.stompClient.send('/app/webrtc.reject', {}, JSON.stringify({
                fromUserId: this.userId,
                toUserId: toUserId,
                appointmentId: appointmentId
            }));
        }
    }

    endCall(toUserId, appointmentId) {
        if (this.connected) {
            this.stompClient.send('/app/webrtc.end', {}, JSON.stringify({
                fromUserId: this.userId,
                toUserId: toUserId,
                appointmentId: appointmentId
            }));
        }
    }

    disconnect() {
        if (this.connected && this.stompClient) {
            this.stompClient.send('/app/chat.offline', {}, this.userId);
            this.stompClient.disconnect(() => {
                console.log('WebSocket已断开');
                this.connected = false;
            });
        }
    }
}

const wsClient = new WebSocketClient();
```

### 4. 在页面中使用

```javascript
// chat.js
let currentUserId = null;
let currentAppointmentId = null;
let receiverUserId = null;

// 初始化
async function initChat() {
    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo'));
    currentUserId = userInfo.userId;

    // 获取预约信息
    currentAppointmentId = getQueryParam('appointmentId');
    receiverUserId = getQueryParam('receiverUserId');

    // 连接WebSocket
    wsClient.onConnected = () => {
        console.log('WebSocket已连接');
        loadHistoryMessages();
    };

    wsClient.onMessageReceived = (message) => {
        displayMessage(message);
    };

    wsClient.connect(currentUserId);
}

// 加载历史消息
async function loadHistoryMessages() {
    const result = await apiClient.getMessages(currentAppointmentId);
    if (result.code === 200) {
        result.data.forEach(message => displayMessage(message));
    }
}

// 发送消息
function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();

    if (!content) return;

    const message = {
        appointmentId: currentAppointmentId,
        senderUserId: currentUserId,
        receiverUserId: receiverUserId,
        messageType: 'TEXT',
        content: content,
        isFromConsultant: userInfo.userType === 'CONSULTANT'
    };

    wsClient.sendMessage(message);
    input.value = '';
}

// 显示消息
function displayMessage(message) {
    const messagesContainer = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = message.isFromConsultant ? 'message consultant' : 'message client';
    messageDiv.textContent = message.content;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// 页面加载时初始化
window.addEventListener('load', initChat);

// 页面卸载时断开连接
window.addEventListener('beforeunload', () => {
    wsClient.disconnect();
});
```

---

## WebRTC视频通话集成

### Web端WebRTC实现

```javascript
// webrtc.js
class WebRTCClient {
    constructor(wsClient) {
        this.wsClient = wsClient;
        this.peerConnection = null;
        this.localStream = null;
        this.remoteStream = null;

        this.configuration = {
            iceServers: [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };
    }

    async startCall(toUserId, appointmentId) {
        try {
            // 获取本地媒体流
            this.localStream = await navigator.mediaDevices.getUserMedia({
                video: true,
                audio: true
            });

            // 显示本地视频
            document.getElementById('localVideo').srcObject = this.localStream;

            // 创建PeerConnection
            this.createPeerConnection();

            // 添加本地流到PeerConnection
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
            });

            // 创建Offer
            const offer = await this.peerConnection.createOffer();
            await this.peerConnection.setLocalDescription(offer);

            // 发送通话请求
            this.wsClient.initiateCall(toUserId, appointmentId);

            // 发送Offer
            this.wsClient.sendWebRTCSignal({
                type: 'offer',
                fromUserId: this.wsClient.userId,
                toUserId: toUserId,
                appointmentId: appointmentId,
                data: offer
            });

        } catch (error) {
            console.error('启动通话失败:', error);
        }
    }

    async answerCall(signal) {
        try {
            // 获取本地媒体流
            this.localStream = await navigator.mediaDevices.getUserMedia({
                video: true,
                audio: true
            });

            document.getElementById('localVideo').srcObject = this.localStream;

            // 创建PeerConnection
            this.createPeerConnection();

            // 添加本地流
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
            });

            // 设置远程描述
            await this.peerConnection.setRemoteDescription(
                new RTCSessionDescription(signal.data)
            );

            // 创建Answer
            const answer = await this.peerConnection.createAnswer();
            await this.peerConnection.setLocalDescription(answer);

            // 发送Answer
            this.wsClient.sendWebRTCSignal({
                type: 'answer',
                fromUserId: this.wsClient.userId,
                toUserId: signal.fromUserId,
                data: answer
            });

        } catch (error) {
            console.error('应答通话失败:', error);
        }
    }

    createPeerConnection() {
        this.peerConnection = new RTCPeerConnection(this.configuration);

        // 处理ICE候选
        this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                this.wsClient.sendWebRTCSignal({
                    type: 'ice-candidate',
                    fromUserId: this.wsClient.userId,
                    toUserId: this.remoteUserId,
                    data: event.candidate
                });
            }
        };

        // 处理远程流
        this.peerConnection.ontrack = (event) => {
            if (!this.remoteStream) {
                this.remoteStream = new MediaStream();
                document.getElementById('remoteVideo').srcObject = this.remoteStream;
            }
            this.remoteStream.addTrack(event.track);
        };

        // 连接状态变化
        this.peerConnection.onconnectionstatechange = () => {
            console.log('连接状态:', this.peerConnection.connectionState);
        };
    }

    async handleSignal(signal) {
        switch (signal.type) {
            case 'call':
                // 收到通话请求
                if (confirm(`收到来自用户${signal.fromUserId}的视频通话请求，是否接受？`)) {
                    this.wsClient.acceptCall(signal.fromUserId, signal.appointmentId);
                } else {
                    this.wsClient.rejectCall(signal.fromUserId, signal.appointmentId);
                }
                break;

            case 'accept':
                // 对方接受通话
                console.log('对方已接受通话');
                break;

            case 'reject':
                // 对方拒绝通话
                alert('对方拒绝了通话');
                this.endCall();
                break;

            case 'offer':
                // 收到Offer
                this.remoteUserId = signal.fromUserId;
                await this.answerCall(signal);
                break;

            case 'answer':
                // 收到Answer
                await this.peerConnection.setRemoteDescription(
                    new RTCSessionDescription(signal.data)
                );
                break;

            case 'ice-candidate':
                // 收到ICE候选
                await this.peerConnection.addIceCandidate(
                    new RTCIceCandidate(signal.data)
                );
                break;

            case 'end':
                // 对方结束通话
                this.endCall();
                break;
        }
    }

    endCall() {
        // 停止本地流
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
        }

        // 关闭PeerConnection
        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }

        // 清空视频元素
        document.getElementById('localVideo').srcObject = null;
        document.getElementById('remoteVideo').srcObject = null;

        this.localStream = null;
        this.remoteStream = null;
    }
}

// 使用示例
const webrtcClient = new WebRTCClient(wsClient);

// 设置WebRTC信令处理
wsClient.onWebRTCSignal = (signal) => {
    webrtcClient.handleSignal(signal);
};

// 发起通话
function initiateVideoCall(toUserId, appointmentId) {
    webrtcClient.startCall(toUserId, appointmentId);
}

// 结束通话
function endVideoCall(toUserId, appointmentId) {
    wsClient.endCall(toUserId, appointmentId);
    webrtcClient.endCall();
}
```

---

## 注意事项

### 1. 服务器地址配置
- 将所有示例中的 `你的服务器IP` 替换为实际的服务器IP地址
- 如果使用域名，确保配置了正确的DNS解析

### 2. HTTPS和WSS
- 生产环境建议使用HTTPS和WSS（安全的WebSocket）
- WebRTC在非localhost环境下需要HTTPS

### 3. 跨域问题
- 服务器已配置CORS，允许所有来源
- 如需限制，修改 `SecurityConfig.java` 中的CORS配置

### 4. Token管理
- Token默认有效期24小时
- 建议实现Token刷新机制
- 在请求失败时检查Token是否过期

### 5. 错误处理
- 所有网络请求都应添加错误处理
- WebSocket断线时实现自动重连
- 视频通话失败时给用户友好提示

### 6. 性能优化
- 聊天消息分页加载
- 图片/视频压缩后上传
- 使用消息队列处理大量并发

---

## 测试建议

1. **单元测试**: 测试API调用和数据解析
2. **集成测试**: 测试WebSocket连接和消息收发
3. **压力测试**: 测试多用户同时在线的性能
4. **网络测试**: 测试弱网环境下的表现

---

## 技术支持

如有问题，请查看：
- 服务器日志: `logs/spring.log`
- 浏览器控制台
- Android Logcat

或联系开发团队获取支持。
