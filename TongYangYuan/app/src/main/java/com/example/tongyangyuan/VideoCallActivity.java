package com.example.tongyangyuan;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.NetworkConfig;
import com.example.tongyangyuan.openim.OpenIMService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import io.livekit.android.room.ConnectOptions;
import io.livekit.android.room.Room;
import io.livekit.android.room.RoomListener;
import io.livekit.android.room.TrackPublication;
import io.livekit.android.room.track.LocalVideoTrack;
import io.livekit.android.room.track.RemoteParticipant;
import io.livekit.android.room.track.RemoteVideoTrack;
import io.livekit.android.room.track.VideoTrack;
import io.livekit.android.view.VideoView;

/**
 * 视频通话 Activity - LiveKit + OpenIM 信令
 *
 * 信令层：OpenIMService（通知对方发起/接听/结束通话）
 * 媒体层：LiveKit SDK（实际音视频传输）
 *
 * 流程：
 * 1. 从后端获取 LiveKit Token
 * 2. 连接 LiveKit Room
 * 3. 发布本地视频、订阅远端视频
 * 4. 通过 OpenIMService 发送/接收通话信令
 */
@OptIn(markerClass = UnstableApi.class)
public class VideoCallActivity extends AppCompatActivity implements RoomListener {

    private static final String TAG = "VideoCallActivity";

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

    // LiveKit
    private Room livekitRoom;
    private String livekitServerUrl;
    private String livekitToken;
    private LocalVideoTrack localVideoTrack;
    private RemoteVideoTrack remoteVideoTrack;

    // OpenIM
    private OpenIMService openIMService;
    private PreferenceStore preferenceStore;

    // UI
    private FrameLayout remoteVideoContainer;
    private FrameLayout localVideoContainer;
    private TextView statusText;
    private ImageButton muteButton;
    private ImageButton videoButton;
    private ImageButton hangupButton;
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
                    startLiveKitRoom();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        extractIntentData();
        initUI();
        initServices();

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
        videoButton = findViewById(R.id.btn_video);
        hangupButton = findViewById(R.id.btn_hangup);

        if (isAudioCall) {
            videoButton.setVisibility(View.GONE);
            localVideoContainer.setVisibility(View.GONE);
        }

        hangupButton.setOnClickListener(v -> endCall());
        muteButton.setOnClickListener(v -> toggleMute());
        videoButton.setOnClickListener(v -> toggleVideo());

        statusText.setText("正在连接 " + consultantName + "...");
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
     * 被叫：接听
     */
    private void acceptCall() {
        openIMService.acceptCall(currentSessionId, callType);
        startLiveKitRoom();
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

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

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
                    mainHandler.post(() -> statusText.setText("视频服务不可用"));
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

        mainHandler.post(() -> statusText.setText("正在建立连接..."));

        String roomName = "apt_" + appointmentId;

        try {
            livekitRoom = io.livekit.android.room.RoomDefaults.createRoom(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create LiveKit room", e);
            mainHandler.post(() -> statusText.setText("LiveKit 创建失败"));
            return;
        }

        ConnectOptions connectOptions = new ConnectOptions(
                livekitServerUrl,
                livekitToken,
                roomName,
                new io.livekit.android.room.options.RoomOptions()
        );

        livekitRoom.connect(connectOptions, this);
    }

    /**
     * 发布本地视频轨道
     */
    private void publishLocalVideo() {
        if (isAudioCall || livekitRoom == null) return;

        try {
            localVideoTrack = livekitRoom.getLocalParticipant().createCameraTrack(new io.livekit.android.room.track.CameraCapturerOptions());
            livekitRoom.getLocalParticipant().publishVideoTrack(localVideoTrack, new io.livekit.android.room.options.VideoTrackPublishOptions());

            mainHandler.post(() -> {
                if (localVideoContainer != null && localVideoTrack != null) {
                    VideoView videoView = new VideoView(this);
                    localVideoTrack.attach(videoView);
                    localVideoContainer.addView(videoView);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to publish local video", e);
        }
    }

    // ==================== RoomListener 实现 ====================

    @Override
    public void onConnected(@NonNull Room room) {
        Log.i(TAG, "LiveKit room connected");
        mainHandler.post(() -> {
            statusText.setText("通话中");
            if (!isAudioCall) {
                publishLocalVideo();
            }
        });
    }

    @Override
    public void onDisconnected(@NonNull Room room, @Nullable Exception exception) {
        Log.i(TAG, "Disconnected from LiveKit room");
    }

    @Override
    public void onParticipantConnected(@NonNull Room room, @NonNull RemoteParticipant participant) {
        Log.i(TAG, "Remote participant connected: " + participant.getIdentity());
        mainHandler.post(() -> {
            statusText.setText("与 " + consultantName + " 通话中");
            // 订阅远端视频
            for (TrackPublication pub : participant.getVideoTracks()) {
                if (pub.getTrack() instanceof RemoteVideoTrack) {
                    remoteVideoTrack = (RemoteVideoTrack) pub.getTrack();
                    showRemoteVideo();
                }
            }
        });
    }

    @Override
    public void onParticipantDisconnected(@NonNull Room room, @NonNull RemoteParticipant participant) {
        Log.i(TAG, "Remote participant disconnected: " + participant.getIdentity());
        mainHandler.post(() -> {
            Toast.makeText(this, consultantName + " 已离开通话", Toast.LENGTH_SHORT).show();
            disconnectRoom();
            finish();
        });
    }

    @Override
    public void onActiveSpeakersChanged(@NonNull Room room, @NonNull java.util.List<RemoteParticipant> speakers) {
        // 可用于说话人高亮
    }

    @Override
    public void onVideoTrackSubscribed(@NonNull Room room, @NonNull RemoteParticipant participant,
                                       @NonNull TrackPublication publication) {
        if (publication.getTrack() instanceof RemoteVideoTrack) {
            remoteVideoTrack = (RemoteVideoTrack) publication.getTrack();
            mainHandler.post(this::showRemoteVideo);
        }
    }

    private void showRemoteVideo() {
        if (remoteVideoContainer != null && remoteVideoTrack != null) {
            remoteVideoContainer.removeAllViews();
            VideoView videoView = new VideoView(this);
            remoteVideoTrack.attach(videoView);
            remoteVideoContainer.addView(videoView);
        }
    }

    @Override
    public void onError(@NonNull Room room, @NonNull Exception exception) {
        Log.e(TAG, "LiveKit error: " + exception.getMessage(), exception);
        mainHandler.post(() -> statusText.setText("通话出错"));
    }

    // ==================== 控制按钮 ====================

    private void toggleMute() {
        if (livekitRoom == null) return;
        isMuted = !isMuted;
        livekitRoom.getLocalParticipant().setMicrophoneEnabled(!isMuted);
        muteButton.setAlpha(isMuted ? 0.5f : 1.0f);
    }

    private void toggleVideo() {
        if (localVideoTrack == null || livekitRoom == null) return;
        isVideoEnabled = !isVideoEnabled;
        localVideoTrack.setEnabled(isVideoEnabled);
        videoButton.setAlpha(isVideoEnabled ? 1.0f : 0.5f);
        localVideoContainer.setVisibility(isVideoEnabled ? View.VISIBLE : View.GONE);
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
        if (livekitRoom != null) {
            try {
                livekitRoom.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Disconnect room error", e);
            }
            livekitRoom = null;
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
