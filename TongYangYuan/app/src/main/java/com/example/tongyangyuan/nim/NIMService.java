package com.example.tongyangyuan.nim;

import android.content.Context;
import android.util.Log;

/**
 * 网易云信NIM服务类（空壳实现）
 * 注意：由于NIM SDK无法下载，此为临时空壳实现
 * 实际IM和通话功能需要下载SDK后才能使用
 */
public class NIMService {
    private static final String TAG = "NIMService";
    private static NIMService instance;
    
    private Context context;
    private boolean isInitialized = false;
    private String currentAccountId;
    
    // 消息回调
    private MessageCallback messageCallback;
    private CallCallback callCallback;
    
    public interface MessageCallback {
        void onMessageReceived(String accountId, String content, String msgType, long timestamp);
    }
    
    public interface CallCallback {
        void onCallReceived(String accountId, String callType, String sessionId);
        void onCallAnswered(String sessionId, boolean accepted);
        void onCallEnded(String sessionId);
    }
    
    private NIMService(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized NIMService getInstance(Context context) {
        if (instance == null) {
            instance = new NIMService(context);
        }
        return instance;
    }
    
    /**
     * 初始化NIM SDK（空壳）
     */
    public void init() {
        if (isInitialized) {
            Log.w(TAG, "NIM already initialized");
            return;
        }
        isInitialized = true;
        Log.i(TAG, "NIM SDK initialized (STUB - SDK not available)");
    }
    
    /**
     * 登录NIM服务器（空壳）
     */
    public void login(String accountId, String token, LoginCallback callback) {
        if (!isInitialized) {
            init();
        }
        
        // 空壳实现：模拟登录成功
        currentAccountId = accountId;
        Log.i(TAG, "NIM login (STUB): " + accountId);
        
        if (callback != null) {
            callback.onSuccess();
        }
    }
    
    /**
     * 退出登录
     */
    public void logout() {
        unregisterMessageListener();
        currentAccountId = null;
        Log.i(TAG, "NIM logged out (STUB)");
    }
    
    /**
     * 发送文本消息（空壳）
     */
    public void sendTextMessage(String targetAccountId, String content) {
        if (!isInitialized || currentAccountId == null) {
            Log.w(TAG, "NIM not logged in, cannot send message");
            return;
        }
        
        Log.i(TAG, "Text message sent (STUB) to: " + targetAccountId + ", content: " + content);
    }
    
    /**
     * 发送图片消息（空壳）
     */
    public void sendImageMessage(String targetAccountId, String imagePath) {
        if (!isInitialized || currentAccountId == null) {
            return;
        }
        
        Log.i(TAG, "Image message sent (STUB) to: " + targetAccountId);
    }
    
    /**
     * 发起音视频通话（空壳）
     */
    public String startCall(String targetAccountId, String callType) {
        if (!isInitialized || currentAccountId == null) {
            Log.w(TAG, "NIM not logged in, cannot start call");
            return null;
        }
        
        String sessionId = System.currentTimeMillis() + "_" + targetAccountId;
        Log.i(TAG, "Starting " + callType + " call (STUB) to: " + targetAccountId);
        
        // 回调通知
        if (callCallback != null) {
            callCallback.onCallReceived(targetAccountId, callType, sessionId);
        }
        
        return sessionId;
    }
    
    /**
     * 接听来电（空壳）
     */
    public void acceptCall(String sessionId, String callType) {
        Log.i(TAG, "Accepting call (STUB): " + sessionId);
    }
    
    /**
     * 拒绝来电（空壳）
     */
    public void rejectCall(String sessionId) {
        Log.i(TAG, "Rejecting call (STUB): " + sessionId);
    }
    
    /**
     * 结束通话（空壳）
     */
    public void endCall() {
        Log.i(TAG, "Call ended (STUB)");
    }
    
    /**
     * 注册消息监听（空壳）
     */
    private void registerMessageListener() {
        Log.i(TAG, "Message listener registered (STUB)");
    }
    
    /**
     * 注销消息监听
     */
    private void unregisterMessageListener() {
        Log.i(TAG, "Message listener unregistered (STUB)");
    }
    
    public void setMessageCallback(MessageCallback callback) {
        this.messageCallback = callback;
    }
    
    public void setCallCallback(CallCallback callback) {
        this.callCallback = callback;
    }
    
    public boolean isLoggedIn() {
        return currentAccountId != null;
    }
    
    public String getCurrentAccountId() {
        return currentAccountId;
    }
    
    public interface LoginCallback {
        void onSuccess();
        void onFailed(int code);
        void onException(Throwable exception);
    }
}
