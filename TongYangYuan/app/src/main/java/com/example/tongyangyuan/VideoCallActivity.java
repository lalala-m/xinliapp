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

/**
 * 视频通话 Activity - LiveKit + OpenIM 信令
 *
 * 信令层：OpenIMService（通知对方发起/接听/结束通话）
 * 媒体层：LiveKit SDK 2.x（由 {@link LiveKitSessionManager} 封装）
 */
public class VideoCallActivity extends AppCompatActivity {

    private static final String TAG = "VideoCallActivity";
    private static final int REQ_MEDIA_PERMISSIONS = 1001;

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

    private String livekitServerUrl;
    private String livekitToken;
    /** 避免 fetch 回调与 onCallAnswered 重复发起 LiveKit 连接 */
    private boolean liveKitConnectStarted;

    private LiveKitSessionManager liveKitSession;

    // OpenIM
    private OpenIMService openIMService;
    private PreferenceStore preferenceStore;

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
                currentSessionId = sessionId;
                VideoCallActivity.this.callType = callType;
                isAudioCall = !"video".equalsIgnoreCase(callType);
                acceptCall();
            });
        }

        @Override
        public void onCallAnswered(String sessionId, boolean accepted) {
            mainHandler.post(() -> {
                if (accepted) {
                    // 主叫已在 startOutgoingCall 里拉 token；被叫在 acceptCall 里拉 token。
                    // 若 token 已就绪但尚未连上，再补一次。
                    if (livekitToken != null && livekitServerUrl != null) {
                        startLiveKitRoom();
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
                statusText.setText("通话出错");
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
        if (!need.isEmpty()) {
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
    }

    /**
     * 主叫：发起通话
     */
    private void startOutgoingCall() {
        statusText.setText("正在呼叫 " + consultantName + "...");
        String targetAccountId = String.valueOf(targetUserId);
        currentSessionId = openIMService.startCall(targetAccountId, callType);
        fetchLiveKitTokenAndConnect();
    }

    /**
     * 被叫：接听（同样需要向后端拉取 LiveKit Token）
     */
    private void acceptCall() {
        openIMService.acceptCall(currentSessionId, callType);
        fetchLiveKitTokenAndConnect();
    }

    /**
     * 从后端获取 LiveKit Token 并连接
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

                    if (token != null && !token.isEmpty() && serverUrl != null && !serverUrl.contains("your-livekit")) {
                        livekitToken = token;
                        livekitServerUrl = serverUrl;
                        mainHandler.post(this::startLiveKitRoom);
                    } else {
                        mainHandler.post(() -> {
                            Toast.makeText(this, "LiveKit 未配置，请先在服务器配置 LiveKit", Toast.LENGTH_SHORT).show();
                            statusText.setText("音视频服务未配置，等待对方接听...");
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

        mainHandler.post(() -> {
            statusText.setText("正在建立连接...");
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
     * 结束通话
     */
    public void endCall() {
        if (openIMService != null) {
            openIMService.endCall();
        }
        disconnectRoom();
        finish();
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
        super.onDestroy();
    }
}
