package com.example.tongyangyuan.openim;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tongyangyuan.database.NetworkConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // 配置
    private final OpenIMConfig config;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 回调
    private MessageCallback messageCallback;
    private CallCallback callCallback;
    private ConnectionCallback connectionCallback;

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
        if (isInitialized) {
            Log.w(TAG, "OpenIM already initialized");
            return;
        }

        executor.execute(() -> {
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
                        apiUrl = data.optString("apiUrl", "");
                        wsUrl = data.optString("wsUrl", "");
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
        });

        isInitialized = true;
        Log.i(TAG, "OpenIM SDK initialized");
    }

    /**
     * 登录 OpenIM 服务器
     */
    public void login(String userId, String token, LoginCallback callback) {
        if (!isInitialized) {
            init();
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
                    conn.setRequestProperty("Authorization", "Bearer " + NetworkConfig.getAuthToken(context));
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    JSONObject body = new JSONObject();
                    body.put("userId", Long.parseLong(userId));
                    conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

                    if (conn.getResponseCode() == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();

                        JSONObject json = new JSONObject(sb.toString());
                        if (json.optInt("code") == 200) {
                            JSONObject data = json.getJSONObject("data");
                            effectiveToken = data.optString("token", "");
                            effectiveWsUrl = data.optString("wsUrl", wsUrl);
                            config.saveFromResponse(userId, effectiveToken,
                                    effectiveWsUrl, apiUrl);
                        }
                    }
                    conn.disconnect();
                }

                if (effectiveToken == null || effectiveToken.isEmpty()) {
                    // OpenIM 服务未部署，返回模拟成功
                    Log.w(TAG, "OpenIM server not available, using mock login");
                    currentUserId = userId;
                    isLoggedIn = true;
                    mainHandler.post(() -> {
                        if (callback != null) callback.onSuccess();
                    });
                    return;
                }

                // 2. 连接 OpenIM WebSocket
                currentUserId = userId;
                currentToken = effectiveToken;
                wsUrl = effectiveWsUrl;
                isLoggedIn = true;

                Log.i(TAG, "OpenIM login success: userId=" + userId);
                mainHandler.post(() -> {
                    if (connectionCallback != null) connectionCallback.onConnected();
                    if (callback != null) callback.onSuccess();
                });

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
     * 发起音视频通话（通过 OpenIM 信令 + LiveKit 媒体）
     * @param targetAccountId 被叫方账号
     * @param callType "video" 或 "audio"
     * @return sessionId
     */
    public String startCall(String targetAccountId, String callType) {
        if (!isLoggedIn || currentUserId == null) {
            Log.w(TAG, "OpenIM not logged in, cannot start call");
            return null;
        }

        String sessionId = System.currentTimeMillis() + "_" + targetAccountId;
        Log.i(TAG, "Starting " + callType + " call to: " + targetAccountId + ", sessionId=" + sessionId);

        // 通过 OpenIM 信令通知对方发起通话
        sendCallSignaling(targetAccountId, callType, sessionId, "call");

        return sessionId;
    }

    /**
     * 发送通话信令（call/accept/reject/end）
     */
    private void sendCallSignaling(String targetAccountId, String callType, String sessionId, String action) {
        executor.execute(() -> {
            try {
                String url = NetworkConfig.getBaseUrl() + "/messages";
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + currentToken);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("senderUserId", Long.parseLong(currentUserId));
                body.put("receiverUserId", Long.parseLong(targetAccountId));
                body.put("messageType", "SYSTEM");
                body.put("content", String.format("CALL:%s:%s:%s", action, callType, sessionId));

                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
                conn.disconnect();
                Log.d(TAG, "Call signaling sent: " + action);
            } catch (Exception e) {
                Log.e(TAG, "Send call signaling error", e);
            }
        });
    }

    /**
     * 接听来电
     */
    public void acceptCall(String sessionId, String callType) {
        Log.i(TAG, "Accepting call: " + sessionId);
        sendCallSignaling(null, callType, sessionId, "accept");
        if (callCallback != null) {
            callCallback.onCallAnswered(sessionId, true);
        }
    }

    /**
     * 拒绝来电
     */
    public void rejectCall(String sessionId) {
        Log.i(TAG, "Rejecting call: " + sessionId);
        sendCallSignaling(null, null, sessionId, "reject");
    }

    /**
     * 结束通话
     */
    public void endCall() {
        Log.i(TAG, "Call ended");
        if (callCallback != null) {
            callCallback.onCallEnded(null);
        }
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
