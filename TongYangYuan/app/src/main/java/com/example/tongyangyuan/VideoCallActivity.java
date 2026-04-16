package com.example.tongyangyuan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.tongyangyuan.data.ChatStore;
import com.example.tongyangyuan.data.ChatMessageRecord;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.NetworkConfig;
import com.example.tongyangyuan.openim.OpenIMService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 视频通话 Activity - LiveKit + OpenIM 信令
 *
 * 信令层：OpenIMService（通知对方发起/接听/结束通话）
 * 媒体层：LiveKit SDK 2.x（由 {@link LiveKitSessionManager} 封装）
 */
public class VideoCallActivity extends AppCompatActivity {

    private static final String TAG = "VideoCallActivity";
    private static final int REQ_MEDIA_PERMISSIONS = 1001;

    /** 通话页关闭时发出，供聊天 WebView 清除「正在呼叫」横幅并刷新记录 */
    public static final String ACTION_VIDEO_CALL_FINISHED = "com.example.tongyangyuan.VIDEO_CALL_FINISHED";

    // Intent Extra Keys
    public static final String KEY_CONSULTANT_NAME = "consultant_name";
    public static final String KEY_APPOINTMENT_ID = "appointment_id";
    public static final String KEY_CURRENT_USER_ID = "current_user_id";
    public static final String KEY_TARGET_USER_ID = "target_user_id";
    public static final String KEY_CALL_TYPE = "call_type";
    public static final String KEY_IS_CALLER = "is_caller";
    public static final String EXTRA_APPOINTMENT_ID = KEY_APPOINTMENT_ID;
    public static final String EXTRA_CURRENT_USER_ID = KEY_CURRENT_USER_ID;
    public static final String EXTRA_TARGET_USER_ID = KEY_TARGET_USER_ID;

    private Long currentUserId;
    private Long targetUserId;
    private Long appointmentId;
    private String callType;
    private String consultantName;
    private boolean isAudioCall;
    private boolean isCaller;
    private String currentSessionId;
    /** 被叫场景下，主叫方用户 ID（用于回发 accept 等信令） */
    private String incomingCallerUserId;

    private String livekitServerUrl;
    private String livekitToken;
    /** 避免 fetch 回调与 onCallAnswered 重复发起 LiveKit 连接 */
    private boolean liveKitConnectStarted;
    /** 权限已授予标志（防止首次启动时请求权限回调晚于 startOutgoingCall） */
    private boolean permissionsGranted = false;
    /** 通话开始时间（毫秒），用于计算通话时长 */
    private long callStartTimeMs = 0;

    private LiveKitSessionManager liveKitSession;

    // OpenIM
    private OpenIMService openIMService;
    private PreferenceStore preferenceStore;
    private ChatStore chatStore;

    // UI
    private FrameLayout remoteVideoContainer;
    private FrameLayout localVideoContainer;
    private TextView statusText;
    private TextView muteLabel;
    private TextView videoLabel;
    private ImageButton muteButton;
    private ImageButton videoButton;
    private ImageButton hangupButton;
    private ImageButton backButton;
    private View avatarWaitingView;
    private View avatarRing1;
    private View avatarRing2;
    private TextView avatarInitial;
    private TextView consultantNameText;
    private TextView waitingHint;
    private boolean isMuted = false;
    private boolean isVideoEnabled = true;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // OpenIM 通话回调
    private final OpenIMService.CallCallback openIMCallCallback = new OpenIMService.CallCallback() {
        @Override
        public void onCallReceived(String accountId, String callType, String sessionId) {
            mainHandler.post(() -> {
                incomingCallerUserId = accountId;
                currentSessionId = sessionId;
                VideoCallActivity.this.callType = callType;
                isAudioCall = !"video".equalsIgnoreCase(callType);
                // 被叫：显示来电弹窗，由用户选择接听/拒绝
                showIncomingCallDialog(accountId, isAudioCall ? "语音来电" : "视频来电");
            });
        }

        @Override
        public void onCallAnswered(String sessionId, boolean accepted) {
            mainHandler.post(() -> {
                cancelCallTimeoutTimer();
                if (accepted) {
                    // 对方接听 → 主叫端开始 LiveKit 连接
                    if (isCaller) {
                        if (livekitToken != null && livekitServerUrl != null && !liveKitConnectStarted) {
                            startLiveKitRoom();
                        } else {
                            // Token 还未拉回，延迟重试（最多 5 秒，每 500ms 查一次）
                            Log.d(TAG, "Token not ready yet, will retry LiveKit connection...");
                            retryLiveKitConnection();
                        }
                    }
                } else {
                    Toast.makeText(VideoCallActivity.this, consultantName + " 拒绝了通话", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        @Override
        public void onCallEnded(String sessionId) {
            mainHandler.post(() -> {
                Toast.makeText(VideoCallActivity.this, "通话已结束", Toast.LENGTH_SHORT).show();
                disconnectRoom();
                finish();
            });
        }
    };

    private final LiveKitSessionManager.Callbacks liveKitCallbacks = new LiveKitSessionManager.Callbacks() {
        @Override
        public void onConnected() {
            mainHandler.post(() -> {
                callStartTimeMs = System.currentTimeMillis();
                if (avatarWaitingView != null) avatarWaitingView.setVisibility(View.GONE);
                if (remoteVideoContainer != null) remoteVideoContainer.setBackgroundColor(0);
                stopRingAnimation();
                statusText.setText("通话中");
            });
        }

        @Override
        public void onRoomDisconnected() {
            mainHandler.post(() -> Log.i(TAG, "LiveKit room disconnected"));
        }

        @Override
        public void onRemoteParticipantLeft() {
            mainHandler.post(() -> {
                Toast.makeText(VideoCallActivity.this, consultantName + " 已离开通话", Toast.LENGTH_SHORT).show();
                disconnectRoom();
                finish();
            });
        }

        @Override
        public void onRemoteVideoReady() {
            mainHandler.post(() ->
                    statusText.setText("与 " + consultantName + " 通话中"));
        }

        @Override
        public void onError(@Nullable String message) {
            mainHandler.post(() -> {
                Log.e(TAG, "LiveKit: " + message);
                String detail = (message != null && !message.isEmpty()) ? message : "连接失败";
                if (detail.length() > 48) {
                    detail = detail.substring(0, 45) + "…";
                }
                statusText.setText("通话出错：" + detail);
                Toast.makeText(VideoCallActivity.this, "LiveKit: " + (message != null ? message : ""), Toast.LENGTH_LONG).show();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        extractIntentData();
        initUI();
        liveKitSession = new LiveKitSessionManager(this, this);
        initServices();

        ensureMediaPermissions();

        if (isCaller) {
            startOutgoingCall();
        }
    }

    private void ensureMediaPermissions() {
        List<String> need = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!isAudioCall && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.CAMERA);
        }
        if (need.isEmpty()) {
            permissionsGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, need.toArray(new String[0]), REQ_MEDIA_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_MEDIA_PERMISSIONS) return;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要摄像头与麦克风权限才能视频通话", Toast.LENGTH_LONG).show();
                return;
            }
        }
        permissionsGranted = true;
        if (isCaller) {
            startOutgoingCall();
        }
    }

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

        if (appointmentId == null || appointmentId <= 0) {
            appointmentId = (System.currentTimeMillis() % 1000000L) + 1L;
            Log.w(TAG, "extractIntentData: invalid appointmentId, using temporary id=" + appointmentId);
        }

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
    }

    private void initUI() {
        remoteVideoContainer = findViewById(R.id.remote_video_container);
        localVideoContainer = findViewById(R.id.local_video_container);
        statusText = findViewById(R.id.status_text);
        muteButton = findViewById(R.id.btn_mute);
        muteLabel = findViewById(R.id.mute_label);
        videoButton = findViewById(R.id.btn_video);
        videoLabel = findViewById(R.id.video_label);
        hangupButton = findViewById(R.id.btn_hangup);
        backButton = findViewById(R.id.btn_back);
        avatarWaitingView = findViewById(R.id.avatarWaitingView);
        avatarRing1 = findViewById(R.id.avatarRing1);
        avatarRing2 = findViewById(R.id.avatarRing2);
        avatarInitial = findViewById(R.id.avatarInitial);
        consultantNameText = findViewById(R.id.consultantNameText);
        waitingHint = findViewById(R.id.waitingHint);

        // 设置头像名字
        if (consultantNameText != null && consultantName != null) {
            consultantNameText.setText(consultantName);
        }
        if (avatarInitial != null && consultantName != null && !consultantName.isEmpty()) {
            avatarInitial.setText(consultantName.substring(0, 1));
        }

        // 音频模式隐藏摄像头按钮
        if (isAudioCall) {
            if (videoButton != null) videoButton.setVisibility(View.GONE);
            if (videoLabel != null) videoLabel.setVisibility(View.GONE);
            if (localVideoContainer != null) localVideoContainer.setVisibility(View.GONE);
        }

        // 返回按钮
        if (backButton != null) {
            backButton.setOnClickListener(v -> endCall());
        }
        hangupButton.setOnClickListener(v -> endCall());
        muteButton.setOnClickListener(v -> toggleMute());
        videoButton.setOnClickListener(v -> toggleVideo());

        // 振铃动画：光环 + 头像抖动
        startRingAnimation();

        statusText.setText("正在连接 " + consultantName + "...");
    }

    private void startRingAnimation() {
        if (avatarRing1 == null || avatarRing2 == null) return;
        avatarRing1.setVisibility(View.VISIBLE);
        avatarRing2.setVisibility(View.VISIBLE);
        android.animation.ObjectAnimator scaleX1 = android.animation.ObjectAnimator.ofFloat(avatarRing1, "scaleX", 1f, 1.5f);
        android.animation.ObjectAnimator scaleY1 = android.animation.ObjectAnimator.ofFloat(avatarRing1, "scaleY", 1f, 1.5f);
        android.animation.ObjectAnimator alpha1 = android.animation.ObjectAnimator.ofFloat(avatarRing1, "alpha", 0.7f, 0f);
        scaleX1.setDuration(2000); scaleY1.setDuration(2000); alpha1.setDuration(2000);
        scaleX1.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleY1.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        alpha1.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        android.animation.AnimatorSet set1 = new android.animation.AnimatorSet();
        set1.playTogether(scaleX1, scaleY1, alpha1);
        set1.start();

        android.animation.ObjectAnimator scaleX2 = android.animation.ObjectAnimator.ofFloat(avatarRing2, "scaleX", 1f, 1.5f);
        android.animation.ObjectAnimator scaleY2 = android.animation.ObjectAnimator.ofFloat(avatarRing2, "scaleY", 1f, 1.5f);
        android.animation.ObjectAnimator alpha2 = android.animation.ObjectAnimator.ofFloat(avatarRing2, "alpha", 0.7f, 0f);
        scaleX2.setDuration(2000); scaleY2.setDuration(2000); alpha2.setDuration(2000);
        scaleX2.setStartDelay(1000); scaleY2.setStartDelay(1000); alpha2.setStartDelay(1000);
        scaleX2.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleY2.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        alpha2.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        android.animation.AnimatorSet set2 = new android.animation.AnimatorSet();
        set2.playTogether(scaleX2, scaleY2, alpha2);
        set2.start();
    }

    private void stopRingAnimation() {
        if (avatarRing1 != null) avatarRing1.setVisibility(View.GONE);
        if (avatarRing2 != null) avatarRing2.setVisibility(View.GONE);
    }

    private void initServices() {
        preferenceStore = new PreferenceStore(this);
        openIMService = OpenIMService.getInstance(this);
        openIMService.setCallCallback(openIMCallCallback);
        openIMService.init();
        // 登录 OpenIM 以建立 WebSocket 连接（接收信令）
        String userId = String.valueOf(currentUserId);
        String token = preferenceStore.getAuthToken();
        openIMService.login(userId, token, new OpenIMService.LoginCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "OpenIM login OK for signaling");
            }

            @Override
            public void onFailed(int code) {
                Log.w(TAG, "OpenIM login failed: " + code + " (will retry on next call)");
            }

            @Override
            public void onException(Throwable exception) {
                Log.e(TAG, "OpenIM login exception", exception);
            }
        });
    }

    /**
     * 主叫：发起通话 → 发送 call 信令 → 等待对方 accept
     */
    private void startOutgoingCall() {
        if (!permissionsGranted) {
            mainHandler.post(() -> statusText.setText("等待权限授权..."));
            return;
        }
        mainHandler.post(this::startRingAnimation);
        statusText.setText("等待 " + consultantName + " 接听...");
        String targetAccountId = String.valueOf(targetUserId);
        long apt = (appointmentId != null && appointmentId > 0) ? appointmentId : ((System.currentTimeMillis() % 1000000L) + 1L);
        currentSessionId = openIMService.startCall(targetAccountId, callType, apt);
        // 拉取 LiveKit Token（等对方 accept 后再连，这里先拉好）
        fetchLiveKitTokenAndConnect();
        // 启动 60 秒超时检测
        startCallTimeoutTimer();
    }

    /**
     * 被叫：接听来电 → 发送 accept 信令 → 连接 LiveKit
     */
    private void acceptCall() {
        mainHandler.post(this::startRingAnimation);
        statusText.setText("正在接听...");
        String peer = incomingCallerUserId != null ? incomingCallerUserId : String.valueOf(targetUserId);
        long apt = (appointmentId != null && appointmentId > 0) ? appointmentId : -1L;
        openIMService.acceptCall(currentSessionId, callType, peer, apt);
        // 拉取 LiveKit Token，若已就绪则立即连接
        fetchLiveKitTokenAndConnect();
        // 若 token 已预拉好，立即连接
        if (livekitToken != null && livekitServerUrl != null) {
            mainHandler.postDelayed(this::startLiveKitRoom, 500);
        }
    }

    /**
     * 从后端获取 LiveKit Token 并存储（不立即连接）
     * 连接时机由信令控制：
     *   - 主叫：收到 accept 信令后
     *   - 被叫：用户在对话框点击"接听"后
     */
    private void fetchLiveKitTokenAndConnect() {
        new Thread(() -> {
            try {
                String room = "apt_" + appointmentId;
                String identity = String.valueOf(currentUserId);
                String urlStr = NetworkConfig.getBaseUrl() + "/livekit/token?room=" + room + "&identity=" + identity;

                HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
                conn.setConnectTimeout(5000);

                int code = conn.getResponseCode();
                java.io.InputStream stream = code == 200 ? conn.getInputStream() : conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                if (stream != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                }

                if (code == 200) {
                    JSONObject json = new JSONObject(sb.toString());
                    String token = json.optString("token", null);
                    String serverUrl = json.optString("serverUrl", null);

                    Log.d(TAG, "Token response - code: " + code + ", token: " + (token != null ? "present(" + token.length() + ")" : "null")
                            + ", serverUrl: " + serverUrl);

                    if (token != null && !token.isEmpty() && serverUrl != null && !serverUrl.contains("your-livekit")) {
                        livekitToken = token;
                        // resolveHost：后端返回的 localhost/127.0.0.1 替换为当前环境的正确主机
                        livekitServerUrl = NetworkConfig.resolveHost(serverUrl);
                        Log.d(TAG, "LiveKit token fetched, stored (will connect on accept signal)");
                        // 注意：不在这里调 startLiveKitRoom()，等信令触发
                    } else {
                        mainHandler.post(() -> {
                            Toast.makeText(this, "LiveKit 未配置，请先在服务器配置 LiveKit", Toast.LENGTH_SHORT).show();
                            statusText.setText("音视频服务未配置");
                        });
                    }
                } else {
                    String detail = sb.length() > 0 ? sb.toString() : ("HTTP " + code);
                    Log.e(TAG, "LiveKit token HTTP " + code + ": " + detail);
                    mainHandler.post(() -> {
                        statusText.setText("视频服务不可用");
                        Toast.makeText(this, "获取通话凭证失败: " + detail, Toast.LENGTH_LONG).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch LiveKit token", e);
                mainHandler.post(() -> statusText.setText("视频服务连接失败"));
            }
        }).start();
    }

    /**
     * 连接 LiveKit Room
     */
    private void startLiveKitRoom() {
        if (livekitToken == null || livekitServerUrl == null) {
            Log.w(TAG, "LiveKit not configured");
            statusText.setText("音视频服务未配置，等待对方接听...");
            return;
        }
        if (liveKitConnectStarted) {
            return;
        }
        liveKitConnectStarted = true;

        Log.d(TAG, "startLiveKitRoom: token=" + (livekitToken.length() > 20 ? "OK" : "BAD")
                + ", serverUrl=" + livekitServerUrl + ", isAudioCall=" + isAudioCall);
        mainHandler.post(() -> {
            statusText.setText("正在建立连接...");
            if (!isAudioCall && localVideoContainer != null) {
                localVideoContainer.setVisibility(View.VISIBLE);
            }
            liveKitSession.connect(
                    livekitServerUrl,
                    livekitToken,
                    isAudioCall,
                    remoteVideoContainer,
                    localVideoContainer,
                    liveKitCallbacks
            );
        });
    }

    /**
     * 重试 LiveKit 连接（主叫收到 accept 但 token 还未拉回时使用）
     */
    private void retryLiveKitConnection() {
        mainHandler.postDelayed(() -> {
            if (livekitToken != null && livekitServerUrl != null && !liveKitConnectStarted) {
                Log.d(TAG, "Token ready now, connecting to LiveKit...");
                startLiveKitRoom();
            } else if (!liveKitConnectStarted) {
                retryLiveKitConnection(); // 再试一次
            }
        }, 500);
    }

    private Runnable callTimeoutRunnable;

    /**
     * 启动呼叫超时检测（60秒无响应自动挂断）
     */
    private void startCallTimeoutTimer() {
        cancelCallTimeoutTimer();
        callTimeoutRunnable = () -> {
            if (!liveKitConnectStarted && !isFinishing()) {
                Log.w(TAG, "Call timeout: no answer within 60s");
                Toast.makeText(VideoCallActivity.this, "无人接听", Toast.LENGTH_SHORT).show();
                endCall();
            }
        };
        mainHandler.postDelayed(callTimeoutRunnable, 60000);
    }

    private void cancelCallTimeoutTimer() {
        if (callTimeoutRunnable != null) {
            mainHandler.removeCallbacks(callTimeoutRunnable);
            callTimeoutRunnable = null;
        }
    }

    private void toggleMute() {
        isMuted = !isMuted;
        liveKitSession.setMicrophoneEnabled(!isMuted);
        muteButton.setAlpha(isMuted ? 0.5f : 1.0f);
        if (muteLabel != null) muteLabel.setText(isMuted ? "取消" : "静音");
    }

    private void toggleVideo() {
        if (isAudioCall) return;
        isVideoEnabled = !isVideoEnabled;
        liveKitSession.setCameraEnabled(isVideoEnabled);
        videoButton.setAlpha(isVideoEnabled ? 1.0f : 0.5f);
        localVideoContainer.setVisibility(isVideoEnabled ? View.VISIBLE : View.GONE);
        if (videoLabel != null) videoLabel.setText(isVideoEnabled ? "关摄像头" : "开摄像头");
    }

    /**
     * 显示来电弹窗（被叫场景）
     */
    private void showIncomingCallDialog(String callerAccountId, String prompt) {
        stopRingAnimation();
        if (avatarWaitingView != null) avatarWaitingView.setVisibility(View.GONE);

        String callerName = consultantName != null ? consultantName : "家长";

        new android.app.AlertDialog.Builder(this)
                .setTitle(callerName)
                .setMessage(prompt)
                .setCancelable(false)
                .setPositiveButton("接听", (dialog, which) -> {
                    acceptCall();
                })
                .setNegativeButton("拒绝", (dialog, which) -> {
                    Toast.makeText(this, "已拒绝通话", Toast.LENGTH_SHORT).show();
                    if (openIMService != null && currentSessionId != null) {
                        openIMService.rejectCall(currentSessionId, callerAccountId, appointmentId != null ? appointmentId : -1L, callType);
                    }
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    // 按返回键 = 拒绝
                    if (openIMService != null && currentSessionId != null) {
                        openIMService.rejectCall(currentSessionId, callerAccountId, appointmentId != null ? appointmentId : -1L, callType);
                    }
                    finish();
                })
                .show();

        // 振铃动画（对话框出现后播放）
        mainHandler.postDelayed(this::startRingAnimation, 300);
    }

    /**
     * 结束通话
     */
    public void endCall() {
        if (openIMService != null && currentSessionId != null && targetUserId != null
                && appointmentId != null && appointmentId > 0) {
            openIMService.notifyPeerCallEnded(String.valueOf(targetUserId), appointmentId, currentSessionId, callType);
        }
        if (openIMService != null) {
            openIMService.endCall();
        }
        disconnectRoom();

        // 通话结束后写入聊天消息（时长信息）
        sendCallEndedMessage();

        finish();
    }

    /**
     * 发送通话结束消息到聊天记录
     */
    private void sendCallEndedMessage() {
        if (appointmentId == null || appointmentId <= 0 || currentUserId == null || targetUserId == null) {
            return;
        }
        final long aptId = this.appointmentId;
        final long callerId = this.currentUserId;
        final long calleeId = this.targetUserId;
        final long startTime = this.callStartTimeMs;
        final long endTime = System.currentTimeMillis();
        final String callTypeStr = this.callType != null ? this.callType : "video";

        new Thread(() -> {
            try {
                // 计算通话时长
                long durationSec = 0;
                if (startTime > 0) {
                    durationSec = (endTime - startTime) / 1000;
                }
                String durationStr = formatDuration(durationSec);

                // 构建 SYSTEM 消息：通话结束通知
                // 格式：CALL_ENDED:video/audio:时长秒数:格式化时长
                String content = "CALL_ENDED:" + callTypeStr + ":" + durationSec + ":" + durationStr;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appointmentId", aptId);
                jsonObject.put("senderUserId", callerId);
                jsonObject.put("receiverUserId", calleeId);
                jsonObject.put("messageType", "SYSTEM");
                jsonObject.put("content", content);
                // 实体 is_from_consultant 非空，省略时服务端持久化会失败，导致聊天里看不到「通话已结束」
                jsonObject.put("isFromConsultant", false);

                String jsonBody = jsonObject.toString();
                String urlStr = NetworkConfig.getBaseUrl() + "/messages";

                HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                String token = preferenceStore.getAuthToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Call ended message sent, HTTP " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to send call ended message", e);
            }
        }).start();
    }

    /**
     * 格式化时长为 MM:SS 或 HH:MM:SS
     */
    private String formatDuration(long seconds) {
        if (seconds < 0) seconds = 0;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
        }
    }

    private void disconnectRoom() {
        liveKitConnectStarted = false;
        if (liveKitSession != null) {
            liveKitSession.disconnect(true);
        }
    }

    @Override
    protected void onDestroy() {
        disconnectRoom();
        if (openIMService != null) {
            openIMService.setCallCallback(null);
        }
        if (isFinishing()) {
            Intent done = new Intent(ACTION_VIDEO_CALL_FINISHED);
            done.setPackage(getPackageName());
            sendBroadcast(done);
        }
        super.onDestroy();
    }
}
