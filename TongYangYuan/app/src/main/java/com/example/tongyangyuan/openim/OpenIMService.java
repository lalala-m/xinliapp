package com.example.tongyangyuan.openim;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tongyangyuan.database.NetworkConfig;
import com.example.tongyangyuan.data.PreferenceStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * OpenIM 服务类 - 替代网易云信 NIM
 * 支持即时通讯、语音/视频通话信令（信令走 OpenIM，媒体流走 LiveKit）
 */
public class OpenIMService {

    private static final String TAG = "OpenIMService";
    private static OpenIMService instance;

    // 状态
    private boolean isInitialized = false;
    private boolean isLoggedIn = false;
    private String currentUserId;
    private String currentToken;
    private String wsUrl;
    private String apiUrl;

    // WebSocket 信令连接（用于实时接收 CALL / ACCEPT / REJECT / END 信令）
    private WebSocket wsClient;
    private boolean wsConnected = false;
    private OkHttpClient wsOkHttpClient;
    private String wsAuthToken;
    private final Object wsLock = new Object();
    /** 累积分片消息，直到 TEXT 帧完整 */
    private StringBuilder wsFragment = new StringBuilder();

    // 配置
    private final OpenIMConfig config;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 回调
    private MessageCallback messageCallback;
    private CallCallback callCallback;
    private ConnectionCallback connectionCallback;
    /** 登录完成前的通话请求暂存，等登录成功后再执行 */
    private final List<Runnable> pendingCalls = new CopyOnWriteArrayList<>();

    // ==================== 回调接口（与原 NIMService 接口兼容） ====================

    public interface LoginCallback {
        void onSuccess();
        void onFailed(int code);
        void onException(Throwable exception);
    }

    public interface MessageCallback {
        void onMessageReceived(String accountId, String content, String msgType, long timestamp);
    }

    public interface CallCallback {
        void onCallReceived(String accountId, String callType, String sessionId);
        void onCallAnswered(String sessionId, boolean accepted);
        void onCallEnded(String sessionId);
    }

    public interface ConnectionCallback {
        void onConnected();
        void onDisconnected();
        void onConnectFailed(Throwable e);
    }

    // ==================== 构造与初始化 ====================

    private OpenIMService(Context context) {
        this.context = context.getApplicationContext();
        this.config = new OpenIMConfig(this.context);
        // 尝试从本地缓存恢复配置
        this.apiUrl = config.getApiUrl();
        this.wsUrl = config.getWsUrl();
        this.currentToken = config.getToken();
        this.currentUserId = config.getUserId();
    }

    public static synchronized OpenIMService getInstance(Context context) {
        if (instance == null) {
            instance = new OpenIMService(context);
        }
        return instance;
    }

    /**
     * 初始化 OpenIM SDK
     * 从后端获取配置后初始化
     */
    public void init() {
        executor.execute(() -> {
            doInit();
        });
    }

    private synchronized void doInit() {
        if (isInitialized) {
            Log.w(TAG, "OpenIM already initialized");
            return;
        }

        try {
            // 从后端获取 OpenIM 配置
            String configUrl = NetworkConfig.getBaseUrl() + "/openim/config";
            Log.d(TAG, "Fetching OpenIM config from: " + configUrl);

            URL url = URI.create(configUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                if (json.optInt("code") == 200) {
                    JSONObject data = json.getJSONObject("data");
                    // resolveHost：将后端返回的 localhost/127.0.0.1 替换为当前环境的正确主机
                    // 模拟器上需要 127.0.0.1 + adb reverse，真机/局域网直接用局域网IP
                    apiUrl = NetworkConfig.resolveHost(data.optString("apiUrl", ""));
                    String backendWsUrl = data.optString("wsUrl", "");
                    wsUrl = NetworkConfig.resolveHost(backendWsUrl);
                    // 确保 wsUrl 包含 /api 前缀（因为后端 context-path=/api）
                    if (wsUrl != null && !wsUrl.contains("/api")) {
                        wsUrl = wsUrl.replaceFirst("^(ws://[^/]+)", "$1/api");
                    }
                    boolean available = data.optBoolean("available", false);

                    config.setApiUrl(apiUrl);
                    config.setWsUrl(wsUrl);
                    config.setAvailable(available);

                    Log.i(TAG, "OpenIM config loaded: api=" + apiUrl + ", ws=" + wsUrl + ", available=" + available);
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load OpenIM config", e);
        }

        isInitialized = true;
        Log.i(TAG, "OpenIM SDK initialized");
    }

    // ==================== WebSocket 信令连接 ====================

    /**
     * 建立 WebSocket 连接，监听 /user/queue/webrtc 信令。
     * 须在获取到 OpenIM Token 后调用（等 wsUrl 和 currentToken 都就绪）。
     */
    private void connectWebSocket() {
        synchronized (wsLock) {
            if (wsConnected && wsClient != null) {
                Log.d(TAG, "WebSocket already connected");
                return;
            }
        }

        String url = wsUrl;
        String token = currentToken;
        if (url == null || url.isEmpty() || token == null || token.isEmpty()) {
            Log.w(TAG, "WebSocket: missing wsUrl or token, skipping WS connect");
            return;
        }

        // 将 HTTP URL 转为 WS/WSS URL
        String wsTarget;
        if (url.startsWith("http://")) {
            wsTarget = url.replaceFirst("http://", "ws://");
        } else if (url.startsWith("https://")) {
            wsTarget = url.replaceFirst("https://", "wss://");
        } else {
            wsTarget = url.startsWith("ws") ? url : "ws://" + url;
        }
        
        // 检查 baseUrl 是否包含 /api，如果是则直接使用当前 URL 作为 wsTarget
        // 因为 baseUrl 已经是 http://127.0.0.1:8080/api，只需要把 http 替换为 ws
        // 如果 wsUrl 已经是完整的 ws://127.0.0.1:8080/api/stomp，直接使用
        if (!wsTarget.contains("/stomp")) {
            // 如果不包含 /stomp，加上 /stomp
            wsTarget = wsTarget.replaceFirst("/+$", "") + "/stomp";
        }
        
        // 去掉末尾可能的斜杠
        wsTarget = wsTarget.replaceAll("/+$", "");

        Log.d(TAG, "Connecting WebSocket to: " + wsTarget);

        if (wsOkHttpClient == null) {
            wsOkHttpClient = new OkHttpClient.Builder()
                    .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .build();
        }
        wsAuthToken = token;

        Request request = new Request.Builder()
                .url(wsTarget)
                .build();

        wsClient = wsOkHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.i(TAG, "WebSocket connected!");
                synchronized (wsLock) {
                    wsConnected = true;
                }
                // 发送 STOMP CONNECT 帧（Spring STOMP 支持 JWT Bearer token）
                // STOMP 协议要求：header 和 body 之间用空行分隔
                webSocket.send("CONNECT\naccept-version:1.2\nAuthorization:Bearer " + wsAuthToken + "\n\n");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "WS recv: " + text.substring(0, Math.min(100, text.length())));
                handleWsMessage(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.i(TAG, "WebSocket closing: " + code + " " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.i(TAG, "WebSocket closed: " + code + " " + reason);
                synchronized (wsLock) {
                    wsConnected = false;
                    wsClient = null;
                }
                // 3 秒后重连
                mainHandler.postDelayed(() -> reconnectWebSocket(), 3000);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable error, Response response) {
                Log.e(TAG, "WebSocket failure", error);
                synchronized (wsLock) {
                    wsConnected = false;
                    wsClient = null;
                }
                // 3 秒后重连
                mainHandler.postDelayed(() -> reconnectWebSocket(), 3000);
            }
        });
    }

    private void handleWsMessage(String msg) {
        if (msg.startsWith("CONNECTED")) {
            // 订阅 /user/queue/webrtc
            wsClient.send("SUBSCRIBE\nid:sub-webrtc\ndestination:/user/queue/webrtc\n\n");
            Log.i(TAG, "STOMP subscribed to /user/queue/webrtc");
            return;
        }

        // 跳过 CONNECTED、HEARTBEAT、空消息
        if (msg.startsWith("CONNECTED") || msg.startsWith("HEARTBEAT") || msg.trim().isEmpty()) {
            return;
        }

        // 解析 STOMP MESSAGE 帧，找到空行后的 body
        // STOMP MESSAGE 帧格式：MESSAGE\nheaders\n\nbody
        // 空行是 header 和 body 的分隔符
        int bodyStart = msg.indexOf("\n\n");
        if (bodyStart < 0) {
            // 整个帧没有 body，跳过
            return;
        }
        String body = msg.substring(bodyStart + 2).trim();
        if (body.isEmpty()) return;

        Log.d(TAG, "WS STOMP body: " + body);

        try {
            JSONObject json = new JSONObject(body);
            String type = json.optString("type", "");
            long fromUserId = json.optLong("fromUserId", 0);
            String fromUserIdStr = fromUserId > 0 ? String.valueOf(fromUserId) : null;
            JSONObject data = json.optJSONObject("data");
            String callType = data != null ? data.optString("callType", "video") : "video";
            String sessionId = data != null ? data.optString("sessionId", "") : "";

            switch (type) {
                case "call":
                    mainHandler.post(() -> {
                        if (callCallback != null) {
                            callCallback.onCallReceived(fromUserIdStr, callType, sessionId);
                        }
                    });
                    break;
                case "accept":
                    mainHandler.post(() -> {
                        if (callCallback != null) {
                            callCallback.onCallAnswered(sessionId, true);
                        }
                    });
                    break;
                case "reject":
                    mainHandler.post(() -> {
                        if (callCallback != null) {
                            callCallback.onCallAnswered(sessionId, false);
                        }
                    });
                    break;
                case "end":
                    mainHandler.post(() -> {
                        if (callCallback != null) {
                            callCallback.onCallEnded(sessionId);
                        }
                    });
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse WS message", e);
        }
    }

    private void reconnectWebSocket() {
        Log.d(TAG, "Reconnecting WebSocket...");
        connectWebSocket();
    }

    /**
     * 断开 WebSocket 连接
     */
    private void disconnectWebSocket() {
        synchronized (wsLock) {
            if (wsClient != null) {
                try {
                    wsClient.close(1000, "normal");
                } catch (Exception e) {
                    Log.e(TAG, "Error closing WS", e);
                }
                wsClient = null;
                wsConnected = false;
            }
        }
    }

    /**
     * 登录 OpenIM 服务器
     */
    public void login(String userId, String token, LoginCallback callback) {
        if (!isInitialized) {
            doInit(); // 同步初始化配置（阻塞等待完成）
        }

        executor.execute(() -> {
            try {
                // 1. 从后端获取 Token（如果本地没有）
                String effectiveToken = token;
                String effectiveWsUrl = wsUrl;

                if (token == null || token.isEmpty()) {
                    // 调用后端 API 获取 OpenIM Token
                    String tokenUrl = NetworkConfig.getBaseUrl() + "/openim/init";
                    Log.d(TAG, "Fetching OpenIM token from: " + tokenUrl);

                    HttpURLConnection conn = (HttpURLConnection) URI.create(tokenUrl).toURL().openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    String jwt = NetworkConfig.getAuthToken(context);
                    if (jwt != null && !jwt.isEmpty()) {
                        conn.setRequestProperty("Authorization", "Bearer " + jwt);
                    }
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    JSONObject body = new JSONObject();
                    body.put("userId", Long.parseLong(userId));
                    conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();

                        JSONObject json = new JSONObject(sb.toString());
                        if (json.optInt("code") == 200) {
                            JSONObject data = json.getJSONObject("data");
                            effectiveToken = data.optString("token", "");
                            String backendWsUrl = data.optString("wsUrl", "");
                            // 确保 wsUrl 包含 /api 前缀（因为后端 context-path=/api）
                            effectiveWsUrl = NetworkConfig.resolveHost(backendWsUrl);
                            if (effectiveWsUrl != null && !effectiveWsUrl.contains("/api")) {
                                effectiveWsUrl = effectiveWsUrl.replaceFirst("^(ws://[^/]+)", "$1/api");
                            }
                            String resolvedApiUrl = NetworkConfig.resolveHost(data.optString("apiUrl", apiUrl));
                            config.saveFromResponse(userId, effectiveToken, effectiveWsUrl, resolvedApiUrl);
                            Log.i(TAG, "OpenIM token obtained from backend, wsUrl=" + effectiveWsUrl);
                        } else {
                            Log.w(TAG, "Backend /openim/init returned code=" + json.optInt("code") + ", msg=" + json.optString("message"));
                        }
                    } else {
                        Log.w(TAG, "Backend /openim/init HTTP " + responseCode);
                    }
                    conn.disconnect();
                }

                if (effectiveToken == null || effectiveToken.isEmpty()) {
                    // OpenIM Server 不可用时，尝试使用 App JWT 连接 Spring STOMP（Spring 支持 JWT Bearer token 认证）
                    String jwtToken = NetworkConfig.getAuthToken(context);
                    if (jwtToken != null && !jwtToken.isEmpty()) {
                        effectiveToken = jwtToken;
                        Log.i(TAG, "OpenIM server unavailable, using App JWT for Spring STOMP");
                    } else {
                        // 既无 OpenIM Token 又无 JWT，进入 Mock 模式（仅本地模拟，无信令）
                        Log.w(TAG, "OpenIM server not available, using mock login");
                        currentUserId = userId;
                        isLoggedIn = true;
                        mainHandler.post(this::flushPendingCalls);
                        mainHandler.post(() -> {
                            if (callback != null) callback.onSuccess();
                        });
                        return;
                    }
                }

                // 2. 连接 OpenIM WebSocket
                currentUserId = userId;
                currentToken = effectiveToken;
                wsUrl = effectiveWsUrl;
                isLoggedIn = true;

                Log.i(TAG, "OpenIM login success: userId=" + userId);
                mainHandler.post(this::flushPendingCalls);
                mainHandler.post(() -> {
                    if (connectionCallback != null) connectionCallback.onConnected();
                    if (callback != null) callback.onSuccess();
                });

                // 登录成功后建立 WebSocket 连接
                connectWebSocket();

            } catch (Exception e) {
                Log.e(TAG, "OpenIM login failed", e);
                isLoggedIn = false;
                mainHandler.post(() -> {
                    if (callback != null) callback.onException(e);
                });
            }
        });
    }

    /**
     * 退出登录
     */
    public void logout() {
        isLoggedIn = false;
        currentUserId = null;
        currentToken = null;
        disconnectWebSocket();
        config.clear();
        Log.i(TAG, "OpenIM logged out");
    }

    /**
     * 发送文本消息（通过后端 HTTP API）
     * OpenIM 自建服务端时使用此方法
     */
    public void sendTextMessage(String targetAccountId, String content) {
        if (!isLoggedIn || currentUserId == null) {
            Log.w(TAG, "OpenIM not logged in, cannot send message");
            return;
        }

        executor.execute(() -> {
            try {
                String url = NetworkConfig.getBaseUrl() + "/messages";
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + currentToken);
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                JSONObject body = new JSONObject();
                body.put("appointmentId", 1L); // 会在上层覆盖
                body.put("senderUserId", Long.parseLong(currentUserId));
                body.put("receiverUserId", Long.parseLong(targetAccountId));
                body.put("messageType", "TEXT");
                body.put("content", content);
                body.put("isFromConsultant", false);

                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

                if (conn.getResponseCode() == 200) {
                    Log.i(TAG, "Message sent to " + targetAccountId + ": " + content);
                } else {
                    Log.e(TAG, "Failed to send message: " + conn.getResponseCode());
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Send message error", e);
            }
        });
    }

    /**
     * 发送图片消息
     */
    public void sendImageMessage(String targetAccountId, String imagePath) {
        if (!isLoggedIn || currentUserId == null) {
            Log.w(TAG, "OpenIM not logged in, cannot send image");
            return;
        }
        Log.i(TAG, "Sending image to " + targetAccountId + ": " + imagePath);
        // 图片上传逻辑（Base64 或 multipart）
    }

    /**
     * 发起音视频通话（HTTP 保存系统消息 + 服务端转发 WebRTC 队列，咨询师端 chat 页才能弹窗）
     *
     * @param targetAccountId 被叫方用户 ID（咨询师 userId）
     * @param callType        "video" 或 "audio"
     * @param appointmentId   预约 ID，须与 chat_messages.appointment_id 一致且 &gt; 0
     * @return sessionId
     */
    public String startCall(String targetAccountId, String callType, long appointmentId) {
        String sessionId = System.currentTimeMillis() + "_" + targetAccountId;
        long sender = resolveSenderUserIdForHttp();
        if (sender <= 0) {
            Log.w(TAG, "startCall: 无本地用户 ID，排队等待 OpenIM 登录后再发信令: " + sessionId);
            pendingCalls.add(() -> {
                long s = resolveSenderUserIdForHttp();
                if (s <= 0) {
                    Log.w(TAG, "startCall: 仍无用户 ID，放弃本条排队信令");
                    return;
                }
                doStartCall(targetAccountId, callType, appointmentId, sessionId, s);
            });
            return sessionId;
        }
        doStartCall(targetAccountId, callType, appointmentId, sessionId, sender);
        return sessionId;
    }

    /**
     * 通话 HTTP 信令（POST /messages）使用 App 登录用户 ID，不得依赖 OpenIM 是否已连上。
     */
    private long resolveSenderUserIdForHttp() {
        try {
            if (currentUserId != null && !currentUserId.isEmpty()) {
                return Long.parseLong(currentUserId);
            }
        } catch (NumberFormatException ignored) {
        }
        long id = new PreferenceStore(context).getUserId();
        return id > 0 ? id : 0L;
    }

    private void doStartCall(String targetAccountId, String callType, long appointmentId, String sessionId, long senderUserId) {
        Log.i(TAG, "Starting " + callType + " call to: " + targetAccountId + ", sessionId=" + sessionId + ", appointmentId=" + appointmentId);

        long effectiveAppointmentId = appointmentId;
        if (effectiveAppointmentId <= 0) {
            effectiveAppointmentId = (System.currentTimeMillis() % 1000000L) + 1L;
            Log.w(TAG, "startCall: invalid appointmentId, using temporary id=" + effectiveAppointmentId);
        }

        sendCallSignaling(
                effectiveAppointmentId,
                senderUserId,
                Long.parseLong(targetAccountId),
                callType != null ? callType : "video",
                sessionId,
                "call",
                false
        );
    }

    private void flushPendingCalls() {
        for (Runnable r : pendingCalls) {
            r.run();
        }
        pendingCalls.clear();
    }

    /**
     * 发送通话信令（call/accept/reject/end），与 TongYangYuan-Web chat.js /queue/webrtc 一致
     */
    private void sendCallSignaling(
            long appointmentId,
            long senderUserId,
            long receiverUserId,
            String callType,
            String sessionId,
            String action,
            boolean isFromConsultant
    ) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String url = NetworkConfig.getBaseUrl() + "/messages";
                conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                String jwt = NetworkConfig.getAuthToken(context);
                if (jwt != null && !jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoOutput(true);

                String ct = callType != null ? callType : "";
                JSONObject body = new JSONObject();
                body.put("appointmentId", appointmentId);
                body.put("senderUserId", senderUserId);
                body.put("receiverUserId", receiverUserId);
                body.put("messageType", "SYSTEM");
                body.put("content", String.format("CALL:%s:%s:%s", action, ct, sessionId));
                body.put("isFromConsultant", isFromConsultant);

                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
                int code = conn.getResponseCode();
                Log.d(TAG, "Call signaling HTTP " + code + " action=" + action);
            } catch (Exception e) {
                Log.e(TAG, "Send call signaling error", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    /**
     * 接听来电（通知对方）；须带主叫 userId 与预约 ID，否则无法写入消息表
     */
    public void acceptCall(String sessionId, String callType, String callerUserId, long appointmentId) {
        Log.i(TAG, "Accepting call: " + sessionId);
        long sender = resolveSenderUserIdForHttp();
        if (sender > 0 && callerUserId != null && appointmentId > 0) {
            sendCallSignaling(
                    appointmentId,
                    sender,
                    Long.parseLong(callerUserId),
                    callType != null ? callType : "video",
                    sessionId,
                    "accept",
                    false
            );
        }
        if (callCallback != null) {
            callCallback.onCallAnswered(sessionId, true);
        }
    }

    /**
     * 兼容旧 JS 接口：无对方/预约信息时不发 HTTP，仅回调本地。
     */
    public void acceptCall(String sessionId, String callType) {
        acceptCall(sessionId, callType, null, 0L);
    }

    /**
     * 拒绝来电
     */
    public void rejectCall(String sessionId, String callerUserId, long appointmentId, String callType) {
        Log.i(TAG, "Rejecting call: " + sessionId);
        long sender = resolveSenderUserIdForHttp();
        if (sender > 0 && callerUserId != null && appointmentId > 0) {
            sendCallSignaling(
                    appointmentId,
                    sender,
                    Long.parseLong(callerUserId),
                    callType != null ? callType : "",
                    sessionId,
                    "reject",
                    false
            );
        }
    }

    public void rejectCall(String sessionId) {
        rejectCall(sessionId, null, 0L, null);
    }

    /**
     * 结束通话（仅本地回调）
     */
    public void endCall() {
        Log.i(TAG, "Call ended");
        if (callCallback != null) {
            callCallback.onCallEnded(null);
        }
    }

    /**
     * 通知对方已挂断（写入 SYSTEM 消息并由服务端转发到 /queue/webrtc）
     */
    public void notifyPeerCallEnded(String peerUserId, long appointmentId, String sessionId, String callType) {
        long sender = resolveSenderUserIdForHttp();
        if (sender <= 0 || peerUserId == null || appointmentId <= 0 || sessionId == null) {
            return;
        }
        sendCallSignaling(
                appointmentId,
                sender,
                Long.parseLong(peerUserId),
                callType != null ? callType : "video",
                sessionId,
                "end",
                false
        );
    }

    /**
     * 接收来自后端的推送消息（由 WebAppInterface 或后台轮询调用）
     */
    public void onMessageReceived(String senderId, String content, String msgType, long timestamp) {
        if (messageCallback != null) {
            messageCallback.onMessageReceived(senderId, content, msgType, timestamp);
        }
    }

    /**
     * 接收来自后端的通话信令
     */
    public void onCallSignalingReceived(String action, String callType, String sessionId, String fromUserId) {
        if (callCallback == null) return;

        switch (action) {
            case "call":
                callCallback.onCallReceived(fromUserId, callType, sessionId);
                break;
            case "accept":
                callCallback.onCallAnswered(sessionId, true);
                break;
            case "reject":
                callCallback.onCallAnswered(sessionId, false);
                break;
            case "end":
                callCallback.onCallEnded(sessionId);
                break;
        }
    }

    // ==================== 公开 setter ====================

    public void setMessageCallback(MessageCallback callback) {
        this.messageCallback = callback;
    }

    public void setCallCallback(CallCallback callback) {
        this.callCallback = callback;
    }

    public void setConnectionCallback(ConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    // ==================== 状态查询 ====================

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getCurrentAccountId() {
        return currentUserId;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
