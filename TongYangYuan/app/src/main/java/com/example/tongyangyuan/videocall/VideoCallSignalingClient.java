package com.example.tongyangyuan.videocall;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

/**
 * 视频通话信令客户端
 * 基于OkHttp WebSocket，原生JSON信令（非STOMP）
 * 从task项目适配而来，集成到TYY项目中
 */
public class VideoCallSignalingClient {
    private static final String TAG = "VideoCallSignaling";
    private SignalingListener listener;
    private String roomId;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WebSocket webSocket;
    private OkHttpClient client;

    public interface SignalingListener {
        void onConnected();
        void onDisconnected();
        void onOffer(String sdp, String fromId);
        void onAnswer(String sdp, String fromId);
        void onIceCandidate(String candidate, String fromId);
        void onCall(String fromId);
        void onAccept(String fromId);  // 对方已接听，可以发送offer
        void onEnd(String fromId);
        void onReject(String fromId);  // 对方拒绝通话
        void onUserCount(int count);
        void onPeerJoined();  // 对方已加入房间
        void onError(String error);
        void onRecordingStarted(String fromId);  // 对方开始录制
        void onRecordingStopped(String fromId);  // 对方停止录制
    }

    public VideoCallSignalingClient(SignalingListener listener) {
        this.listener = listener;
    }

    /**
     * 连接信令服务器
     * @param serverUrl 服务器地址，如 ws://192.168.1.100:8080/api/video-signaling?roomId=123
     */
    public void connect(String serverUrl) {
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket connected");
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onConnected();
                    }
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String message) {
                Log.d(TAG, "Received: " + message);
                mainHandler.post(() -> {
                    handleMessage(message);
                });
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onDisconnected();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage());
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(t.getMessage());
                    }
                });
            }
        });
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    // 发送 offer - 直接传 sdp 字符串
    public void sendOffer(String sdp) {
        sendMessageInternal(createMessage("offer", sdp));
    }

    // 发送 answer - 直接传 sdp 字符串
    public void sendAnswer(String sdp) {
        sendMessageInternal(createMessage("answer", sdp));
    }

    // 发送 ICE candidate - 传 JSONObject 对象
    public void sendIceCandidate(JSONObject candidateJson) {
        sendMessageInternal(createMessage("candidate", candidateJson));
    }

    // 发送 call
    public void sendCall() {
        sendMessageInternal(createMessage("call", null));
    }

    // 发送 call（带通话类型）
    public void sendCall(String callType) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "call");
            if (callType != null) {
                json.put("callType", callType);
            }
            sendMessageInternal(json.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creating call message: " + e.getMessage());
        }
    }

    // 发送 accept（被叫接听）
    public void sendAccept() {
        sendMessageInternal(createMessage("accept", null));
    }

    // 发送 end
    public void sendEnd() {
        sendMessageInternal(createMessage("end", null));
    }

    // 发送录制通知
    public void sendRecordingNotification(boolean started) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", started ? "recordingStarted" : "recordingStopped");
            json.put("timestamp", System.currentTimeMillis());
            sendMessageInternal(json.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creating recording notification: " + e.getMessage());
        }
    }

    // 统一的 createMessage 方法
    private String createMessage(String type, Object data) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            if (data != null) {
                // 统一使用 sdp 字段名处理 offer/answer
                if (type.equals("offer") || type.equals("answer")) {
                    json.put("sdp", data);
                } else {
                    json.put(type, data);
                }
            }
            return json.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error creating message: " + e.getMessage());
            return "";
        }
    }

    private void sendMessageInternal(String message) {
        if (message.isEmpty() || webSocket == null) return;
        Log.d(TAG, "Sending: " + message);
        webSocket.send(message);
    }

    // 统一 handleMessage 方法
    private void handleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            if (listener == null) return;

            switch (type) {
                case "userCount":
                    int count = json.getInt("count");
                    listener.onUserCount(count);
                    break;
                case "peerJoined":
                    listener.onPeerJoined();
                    break;
                case "offer":
                    String offerSdp = json.getString("sdp");
                    String fromIdOffer = json.optString("fromId", "");
                    listener.onOffer(offerSdp, fromIdOffer);
                    break;
                case "answer":
                    String answerSdp = json.getString("sdp");
                    String fromIdAnswer = json.optString("fromId", "");
                    listener.onAnswer(answerSdp, fromIdAnswer);
                    break;
                case "candidate":
                    // candidate字段可能是字符串或JSONObject
                    Object candidateObj = json.get("candidate");
                    String candidateStr;
                    if (candidateObj instanceof JSONObject) {
                        candidateStr = candidateObj.toString();
                    } else {
                        candidateStr = candidateObj.toString();
                    }
                    String fromIdCandidate = json.optString("fromId", "");
                    listener.onIceCandidate(candidateStr, fromIdCandidate);
                    break;
                case "call":
                    String fromIdCall = json.optString("fromId", "");
                    listener.onCall(fromIdCall);
                    break;
                case "accept":
                    String fromIdAccept = json.optString("fromId", "");
                    listener.onAccept(fromIdAccept);
                    break;
                case "end":
                    String fromIdEnd = json.optString("fromId", "");
                    listener.onEnd(fromIdEnd);
                    break;
                case "reject":
                    String fromIdReject = json.optString("fromId", "");
                    listener.onReject(fromIdReject);
                    break;
                case "recordingStarted":
                    String fromIdRecStart = json.optString("fromId", "");
                    listener.onRecordingStarted(fromIdRecStart);
                    break;
                case "recordingStopped":
                    String fromIdRecStop = json.optString("fromId", "");
                    listener.onRecordingStopped(fromIdRecStop);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
            webSocket = null;
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }

    public boolean isConnected() {
        return webSocket != null;
    }
}
