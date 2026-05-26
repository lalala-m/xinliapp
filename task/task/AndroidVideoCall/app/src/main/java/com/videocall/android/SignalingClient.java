package com.videocall.android;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class SignalingClient {
    private static final String TAG = "SignalingClient";
    private SignalingListener listener;
    private String targetId;
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
        void onEnd(String fromId);
        void onUserCount(int count);
        void onError(String error);
    }

    public SignalingClient(SignalingListener listener) {
        this.listener = listener;
    }

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
                Log.d(TAG, "【收到完整消息】" + message);
                mainHandler.post(() -> {
                    if (listener != null && listener instanceof MainActivity) {
                        ((MainActivity) listener).addLog("【收到完整消息】" + message);
                    }
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

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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

    // 发送 end
    public void sendEnd() {
        sendMessageInternal(createMessage("end", null));
    }

    // 统一的 createMessage 方法
    private String createMessage(String type, Object data) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            if (targetId != null) {
                json.put("targetId", targetId);
            }
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
        Log.d(TAG, "【发送完整消息】" + message);
        if (listener != null && listener instanceof MainActivity) {
            ((MainActivity) listener).addLog("【发送完整消息】" + message);
        }
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
                    String candidate = json.getString("candidate");
                    String fromIdCandidate = json.optString("fromId", "");
                    listener.onIceCandidate(candidate, fromIdCandidate);
                    break;
                case "call":
                    String fromIdCall = json.optString("fromId", "");
                    listener.onCall(fromIdCall);
                    break;
                case "end":
                    String fromIdEnd = json.optString("fromId", "");
                    listener.onEnd(fromIdEnd);
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
}
