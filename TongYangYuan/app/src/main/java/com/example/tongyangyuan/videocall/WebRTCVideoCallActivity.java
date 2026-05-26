package com.example.tongyangyuan.videocall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tongyangyuan.R;
import android.net.Uri;

import com.example.tongyangyuan.data.ChatMessageRecord;
import com.example.tongyangyuan.data.ChatStore;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.NetworkConfig;

import org.json.JSONObject;
import org.webrtc.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * WebRTC 视频通话 Activity
 * 基于 task 项目的原生 WebRTC 实现，使用简化信令协议
 * 替代 LiveKit + OpenIM 方案
 *
 * 信令流程：
 * 1. connect -> 连接信令服务器
 * 2. call -> 发起通话
 * 3. offer -> 发送 SDP offer
 * 4. answer -> 发送 SDP answer
 * 5. candidate -> 交换 ICE candidate
 * 6. end -> 结束通话
 */
public class WebRTCVideoCallActivity extends AppCompatActivity
        implements VideoCallSignalingClient.SignalingListener {

    private static final String TAG = "WebRTCVideoCall";
    private static final int REQ_MEDIA_PERMISSIONS = 1001;

    public static final String ACTION_VIDEO_CALL_FINISHED = "com.example.tongyangyuan.VIDEO_CALL_FINISHED";

    // Intent Extra Keys
    public static final String KEY_CONSULTANT_NAME = "consultant_name";
    public static final String KEY_APPOINTMENT_ID = "appointment_id";
    public static final String KEY_CURRENT_USER_ID = "current_user_id";
    public static final String KEY_TARGET_USER_ID = "target_user_id";
    public static final String KEY_CALL_TYPE = "call_type";
    public static final String KEY_IS_CALLER = "is_caller";

    private Long currentUserId;
    private Long targetUserId;
    private Long appointmentId;
    private String callType;
    private String consultantName;
    private boolean isAudioCall;
    private boolean isCaller;

    // WebRTC
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private VideoTrack remoteVideoTrack;
    private EglBase eglBase;

    // 信令
    private VideoCallSignalingClient signalingClient;

    // 状态
    private boolean isInCall = false;
    private boolean permissionsGranted = false;
    private String pendingOffer = null;
    private List<IceCandidate> candidateQueue = new ArrayList<>();
    private boolean remoteDescriptionSet = false;
    private long callStartTimeMs = 0;

    // UI
    private FrameLayout remoteVideoContainer;
    private FrameLayout localVideoContainer;
    private TextView statusText;
    private TextView peerStatusText;  // 🔧 新增：对方在线状态
    private ImageButton muteButton;
    private ImageButton videoButton;
    private ImageButton hangupButton;
    private View avatarWaitingView;
    private TextView consultantNameText;
    private boolean isMuted = false;
    private boolean isVideoEnabled = true;

    // 视频录制相关
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder = null;
    private String recordingFilePath = null;
    private File outputDir = null;
    private View recordingIndicator = null;
    private View recordingDot = null;
    private TextView recordingText = null;
    private long recordingStartTime = 0;
    private Handler recordingTimerHandler = null;
    private Runnable recordingTimerRunnable = null;
    
    // MediaProjection 屏幕录制
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private static final int REQUEST_MEDIA_PROJECTION = 1002;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        extractIntentData();
        initUI();
        ensureMediaPermissions();

        if (permissionsGranted) {
            initializeWebRTC();
            createLocalTracks();
            connectSignalingServer();
        }
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            appointmentId = intent.getLongExtra(KEY_APPOINTMENT_ID, -1L);
            currentUserId = intent.getLongExtra(KEY_CURRENT_USER_ID, -1L);
            targetUserId = intent.getLongExtra(KEY_TARGET_USER_ID, -1L);
            callType = intent.getStringExtra(KEY_CALL_TYPE);
            consultantName = intent.getStringExtra(KEY_CONSULTANT_NAME);
            isCaller = intent.getBooleanExtra(KEY_IS_CALLER, false);
            isAudioCall = "audio".equalsIgnoreCase(callType);
        }

        if (appointmentId == null || appointmentId <= 0) {
            appointmentId = (System.currentTimeMillis() % 1000000L) + 1L;
        }

        if (currentUserId == -1L) {
            Toast.makeText(this, "通话数据无效：缺少当前用户ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (consultantName == null) {
            consultantName = "咨询师";
        }

        Log.d(TAG, "extractIntentData: appointmentId=" + appointmentId
                + ", currentUserId=" + currentUserId
                + ", targetUserId=" + targetUserId
                + ", isCaller=" + isCaller);
    }

    private void initUI() {
        remoteVideoContainer = findViewById(R.id.remote_video_container);
        localVideoContainer = findViewById(R.id.local_video_container);
        statusText = findViewById(R.id.status_text);
        peerStatusText = findViewById(R.id.peer_status_text);  // 🔧 新增
        muteButton = findViewById(R.id.btn_mute);
        videoButton = findViewById(R.id.btn_video);
        hangupButton = findViewById(R.id.btn_hangup);
        avatarWaitingView = findViewById(R.id.avatarWaitingView);
        consultantNameText = findViewById(R.id.consultantNameText);

        // 录制状态指示器
        recordingIndicator = findViewById(R.id.recordingIndicator);
        recordingDot = findViewById(R.id.recordingDot);
        recordingText = findViewById(R.id.recordingText);

        if (consultantNameText != null && consultantName != null) {
            consultantNameText.setText(consultantName);
        }

        // 音频模式隐藏摄像头
        if (isAudioCall) {
            if (videoButton != null) videoButton.setVisibility(View.GONE);
            if (localVideoContainer != null) localVideoContainer.setVisibility(View.GONE);
            // 🔧 修复：同时隐藏摄像头按钮下方的文字标签
            TextView videoLabel = findViewById(R.id.video_label);
            if (videoLabel != null) videoLabel.setVisibility(View.GONE);
        }

        hangupButton.setOnClickListener(v -> endCall());
        muteButton.setOnClickListener(v -> toggleMute());
        videoButton.setOnClickListener(v -> toggleVideo());

        statusText.setText(isCaller ? "正在连接 " + consultantName + "..." : "等待来电...");
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_MEDIA_PERMISSIONS) return;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要摄像头与麦克风权限", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        permissionsGranted = true;
        initializeWebRTC();
        createLocalTracks();
        connectSignalingServer();
    }

    // ==================== WebRTC 初始化 ====================

    private void initializeWebRTC() {
        try {
            PeerConnectionFactory.InitializationOptions options =
                    PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
            PeerConnectionFactory.initialize(options);

            PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
            if (!isAudioCall) {
                eglBase = EglBase.create();
                EglBase.Context eglContext = eglBase.getEglBaseContext();
                builder.setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglContext, true, true));
                builder.setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglContext));
            }

            peerConnectionFactory = builder.createPeerConnectionFactory();

            // 初始化录制输出目录
            outputDir = new File(getExternalFilesDir(null), "video_recordings");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            Log.d(TAG, "WebRTC initialized (audioCall=" + isAudioCall + ")");
        } catch (Exception e) {
            Log.e(TAG, "WebRTC init failed", e);
            Toast.makeText(this, "WebRTC初始化失败", Toast.LENGTH_LONG).show();
        }
    }

    // ==================== 信令连接 ====================

    private void connectSignalingServer() {
        try {
            String baseUrl = NetworkConfig.getBaseUrl();
            // 🔧 修复：添加 userId 参数，让服务器可以识别同一用户的重复连接
            // 修正WebSocket路径：确保使用/api/video-signaling（服务器context-path是/api）
            long userId = currentUserId != null ? currentUserId : 0L;
            String wsUrl = baseUrl.replace("http://", "ws://")
                    .replace("https://", "wss://")
                    .replaceFirst("/api(/)?.*", "/api/video-signaling?roomId=" + appointmentId + "&userId=" + userId);

            Log.d(TAG, "Connecting to signaling: " + wsUrl);
            signalingClient = new VideoCallSignalingClient(this);
            signalingClient.connect(wsUrl);
        } catch (Exception e) {
            Log.e(TAG, "Connect signaling failed", e);
            statusText.setText("信令连接失败");
        }
    }

    // ==================== SignalingListener 回调 ====================

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            if (isCaller) {
                statusText.setText("已连接，等待对方接入...");
            } else {
                statusText.setText("已连接，等待来电...");
                // 不要在这里设置 isInCall=true！
                // 只有当用户点击接听后才设置为 true
                Log.d(TAG, "Callee connected, waiting for incoming call...");
            }
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            statusText.setText("连接已断开");
            endCall();
        });
    }

    @Override
    public void onCall(String fromId) {
        Log.d(TAG, "onCall from " + fromId + ", isCaller=" + isCaller + ", isInCall=" + isInCall);
        runOnUiThread(() -> {
            if (!isCaller) {
                if (isInCall) {
                    Log.d(TAG, "Callee already accepted, auto-sending accept");
                    if (callStartTimeMs == 0) {
                        callStartTimeMs = System.currentTimeMillis(); // 🔧 修复：记录通话开始时间
                    }
                    statusText.setText("正在建立连接...");
                    signalingClient.sendAccept();
                    if (pendingOffer != null) {
                        Log.d(TAG, "Processing pending offer");
                        handleOfferInternal(pendingOffer);
                        pendingOffer = null;
                    }
                } else {
                    // 🔧 修复：如果从聊天页面已点击接听（accepted=1），则自动接听，不弹窗
                    // 只有直接从视频通话页面进入且未接听时才显示弹窗
                    Log.d(TAG, "Callee received call, auto-accepting (from chat page)");
                    isInCall = true;
                    callStartTimeMs = System.currentTimeMillis(); // 🔧 修复：记录通话开始时间
                    statusText.setText("正在建立连接...");
                    signalingClient.sendAccept();
                    if (pendingOffer != null) {
                        Log.d(TAG, "Processing pending offer");
                        handleOfferInternal(pendingOffer);
                        pendingOffer = null;
                    }
                }
            }
        });
    }

    @Override
    public void onAccept(String fromId) {
        Log.d(TAG, "onAccept from " + fromId);
        if (isCaller && !isInCall) {
            runOnUiThread(() -> {
                statusText.setText("对方已接听，建立连接中...");
                startCall();
            });
        }
    }

    @Override
    public void onOffer(String sdp, String fromId) {
        Log.d(TAG, "onOffer from " + fromId + ", sdp length=" + (sdp != null ? sdp.length() : 0));
        runOnUiThread(() -> {
            if (!isCaller) {
                if (isInCall) {
                    handleOfferInternal(sdp);
                } else {
                    Log.d(TAG, "Offer received before acceptance, caching");
                    pendingOffer = sdp;
                    if (!isInCall) {
                        onCall(fromId);
                    }
                }
            }
        });
    }

    @Override
    public void onAnswer(String sdp, String fromId) {
        Log.d(TAG, "onAnswer from " + fromId + ", sdp length=" + (sdp != null ? sdp.length() : 0));
        if (peerConnection != null) {
            Log.d(TAG, "Setting remote description (answer)");
            peerConnection.setRemoteDescription(new SimpleSdpObserver(),
                    new SessionDescription(SessionDescription.Type.ANSWER, sdp));
            remoteDescriptionSet = true;
            flushCandidateQueue();
            Log.d(TAG, "Remote description set, connection state=" + peerConnection.connectionState());
        } else {
            Log.e(TAG, "peerConnection is null when receiving answer!");
        }
    }

    @Override
    public void onIceCandidate(String candidate, String fromId) {
        Log.d(TAG, "onCandidate from " + fromId);
        if (peerConnection != null) {
            try {
                JSONObject json = new JSONObject(candidate);
                String candidateStr = json.optString("candidate", "");
                String sdpMid = json.optString("sdpMid", "");
                int sdpMLineIndex = json.optInt("sdpMLineIndex", 0);

                if (candidateStr.isEmpty()) return;

                IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidateStr);
                if (remoteDescriptionSet) {
                    peerConnection.addIceCandidate(iceCandidate);
                } else {
                    candidateQueue.add(iceCandidate);
                }
            } catch (Exception e) {
                Log.e(TAG, "handle candidate error", e);
            }
        }
    }

    @Override
    public void onEnd(String fromId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "对方已结束通话", Toast.LENGTH_SHORT).show();
            endCall();
        });
    }

    @Override
    public void onReject(String fromId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "对方已拒绝通话", Toast.LENGTH_SHORT).show();
            endCall();
        });
    }

    @Override
    public void onUserCount(int count) {
        Log.d(TAG, "Room user count: " + count);
        final boolean isPeerOnline = count >= 2;
        runOnUiThread(() -> {
            if (peerStatusText != null) {
                if (isPeerOnline) {
                    peerStatusText.setText("对方已在线");
                    peerStatusText.setTextColor(android.graphics.Color.parseColor("#4ADE80"));
                } else {
                    peerStatusText.setText("对方未在线");
                    peerStatusText.setTextColor(android.graphics.Color.parseColor("#FF4444"));
                }
            }
        });
    }

    @Override
    public void onPeerJoined() {
        Log.d(TAG, "Peer joined room");
        if (isCaller) {
            runOnUiThread(() -> {
                if (!isInCall) {
                    Log.d(TAG, "Sending call request to peer, callType=" + callType);
                    statusText.setText("正在呼叫对方...");
                    signalingClient.sendCall(callType);
                } else {
                    Log.d(TAG, "Peer rejoined, resending offer");
                    if (peerConnection != null) {
                        peerConnection.createOffer(new SimpleSdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                Log.d(TAG, "Offer recreated, sdp length=" + sessionDescription.description.length());
                                final SessionDescription offer = sessionDescription;
                                peerConnection.setLocalDescription(new SimpleSdpObserver() {
                                    @Override
                                    public void onSetSuccess() {
                                        Log.d(TAG, "Local description set, resending offer");
                                        signalingClient.sendOffer(offer.description);
                                    }
                                }, offer);
                            }
                        }, new MediaConstraints());
                    }
                }
            });
        }
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "Signaling error: " + error);
            statusText.setText("错误: " + error);
        });
    }

    @Override
    public void onRecordingStarted(String fromId) {
        Log.d(TAG, "Recording started notification from: " + fromId);
        runOnUiThread(() -> {
            // 🔧 修改：仅电脑端（咨询师）录制，手机端只显示提示
            Toast.makeText(this, "咨询师已开始录制通话", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onRecordingStopped(String fromId) {
        Log.d(TAG, "Recording stopped notification from: " + fromId);
        runOnUiThread(() -> {
            Toast.makeText(this, "对方已停止录制", Toast.LENGTH_SHORT).show();
        });
    }

    // ==================== 通话控制 ====================

    private void startCall() {
        try {
            Log.d(TAG, "startCall: creating offer");
            isInCall = true;
            callStartTimeMs = System.currentTimeMillis();
            candidateQueue.clear();
            remoteDescriptionSet = false;

            if (localVideoTrack != null) localVideoTrack.setEnabled(true);
            if (localAudioTrack != null) localAudioTrack.setEnabled(true);
            statusText.setText("正在建立连接...");

            MediaConstraints constraints = new MediaConstraints();
            if (!isAudioCall) {
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
            }
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));

            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
            iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
            iceServers.add(PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer());

            PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(iceServers);
            config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
            config.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;

            peerConnection = peerConnectionFactory.createPeerConnection(config, new PeerConnectionObserver() {
                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    try {
                        JSONObject candidateJson = new JSONObject();
                        candidateJson.put("candidate", iceCandidate.sdp);
                        candidateJson.put("sdpMid", iceCandidate.sdpMid);
                        candidateJson.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                        signalingClient.sendIceCandidate(candidateJson);
                    } catch (Exception e) {
                        Log.e(TAG, "Send candidate failed", e);
                    }
                }

                @Override
                public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                    if (rtpReceiver.track() instanceof VideoTrack) {
                        remoteVideoTrack = (VideoTrack) rtpReceiver.track();
                        runOnUiThread(() -> {
                            if (remoteVideoContainer != null && eglBase != null) {
                                SurfaceViewRenderer remoteView = new SurfaceViewRenderer(WebRTCVideoCallActivity.this);
                                remoteView.init(eglBase.getEglBaseContext(), null);
                                remoteView.setMirror(false);
                                remoteView.setEnableHardwareScaler(true);
                                // 远端视频需要设置 zOrderMediaOverlay 为 true 才能显示在本地预览之上
                                remoteView.setZOrderMediaOverlay(true);
                                
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                );
                                remoteView.setLayoutParams(params);
                                
                                remoteVideoTrack.addSink(remoteView);
                                remoteVideoContainer.removeAllViews();
                                remoteVideoContainer.addView(remoteView);
                                Log.d(TAG, "Remote video view added to container (zOrder=true)");
                            }
                            statusText.setText("与 " + consultantName + " 通话中");
                            if (avatarWaitingView != null) avatarWaitingView.setVisibility(View.GONE);
                            
                            // 🔧 仅电脑端（咨询师）录制，手机端不录制
                            Log.d(TAG, "Call connected, recording is handled by counselor side only");
                        });
                    } else if (rtpReceiver.track() instanceof AudioTrack) {
                        runOnUiThread(() -> {
                            statusText.setText("与 " + consultantName + " 通话中");
                            if (avatarWaitingView != null) avatarWaitingView.setVisibility(View.GONE);
                            Log.d(TAG, "Remote audio track received, call connected");
                            
                            // 🔧 仅电脑端（咨询师）录制，手机端不录制
                            Log.d(TAG, "Audio call connected, recording is handled by counselor side only");
                        });
                    }
                }

                @Override
                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                    Log.d(TAG, "Connection state: " + newState);
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                        runOnUiThread(() -> statusText.setText("通话中"));
                    } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED ||
                            newState == PeerConnection.PeerConnectionState.FAILED) {
                        // 🔧 修复：避免重复调用 endCall()
                        if (!isEndingCall) {
                            runOnUiThread(() -> endCall());
                        }
                    }
                }
            });

            List<String> streamIds = new ArrayList<>();
            streamIds.add("stream");
            if (peerConnection != null && localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack, streamIds);
            }
            if (peerConnection != null && localAudioTrack != null) {
                peerConnection.addTrack(localAudioTrack, streamIds);
            }

            peerConnection.createOffer(new SimpleSdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    Log.d(TAG, "Offer created, sdp length=" + sessionDescription.description.length());
                    final SessionDescription offer = sessionDescription;
                    peerConnection.setLocalDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(TAG, "Local description set, sending offer");
                            signalingClient.sendOffer(offer.description);
                        }
                    }, offer);
                }
            }, constraints);

            statusText.setText("等待对方接听...");
        } catch (Exception e) {
            Log.e(TAG, "Start call failed", e);
            Toast.makeText(this, "发起通话失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPeerConnectionForAnswer() {
        try {
            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
            iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
            iceServers.add(PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer());

            PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(iceServers);
            config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

            peerConnection = peerConnectionFactory.createPeerConnection(config, new PeerConnectionObserver() {
                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    try {
                        JSONObject candidateJson = new JSONObject();
                        candidateJson.put("candidate", iceCandidate.sdp);
                        candidateJson.put("sdpMid", iceCandidate.sdpMid);
                        candidateJson.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                        signalingClient.sendIceCandidate(candidateJson);
                    } catch (Exception e) {
                        Log.e(TAG, "Send candidate failed", e);
                    }
                }

                @Override
                public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                    if (rtpReceiver.track() instanceof VideoTrack) {
                        remoteVideoTrack = (VideoTrack) rtpReceiver.track();
                        runOnUiThread(() -> {
                            if (remoteVideoContainer != null && eglBase != null) {
                                SurfaceViewRenderer remoteView = new SurfaceViewRenderer(WebRTCVideoCallActivity.this);
                                remoteView.init(eglBase.getEglBaseContext(), null);
                                remoteView.setMirror(false);
                                remoteView.setEnableHardwareScaler(true);
                                // 远端视频需要设置 zOrderMediaOverlay 为 true 才能显示在本地预览之上
                                remoteView.setZOrderMediaOverlay(true);
                                
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                );
                                remoteView.setLayoutParams(params);
                                
                                remoteVideoTrack.addSink(remoteView);
                                remoteVideoContainer.removeAllViews();
                                remoteVideoContainer.addView(remoteView);
                                Log.d(TAG, "Remote video view added to container (callee, zOrder=true)");
                            }
                            statusText.setText("与 " + consultantName + " 通话中");
                            if (avatarWaitingView != null) avatarWaitingView.setVisibility(View.GONE);
                            
                            // 🔧 仅电脑端（咨询师）录制，手机端不录制
                            Log.d(TAG, "Callee call connected, recording is handled by counselor side only");
                        });
                    } else if (rtpReceiver.track() instanceof AudioTrack) {
                        runOnUiThread(() -> {
                            statusText.setText("与 " + consultantName + " 通话中");
                            if (avatarWaitingView != null) avatarWaitingView.setVisibility(View.GONE);
                            Log.d(TAG, "Remote audio track received (callee), call connected");
                            
                            // 🔧 仅电脑端（咨询师）录制，手机端不录制
                            Log.d(TAG, "Callee audio call connected, recording is handled by counselor side only");
                        });
                    }
                }

                @Override
                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                    Log.d(TAG, "Callee connection state: " + newState);
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                        runOnUiThread(() -> statusText.setText("通话中"));
                    } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED ||
                            newState == PeerConnection.PeerConnectionState.FAILED) {
                        if (!isEndingCall) {
                            runOnUiThread(() -> endCall());
                        }
                    }
                }
            });

            List<String> streamIds = new ArrayList<>();
            streamIds.add("stream");
            if (peerConnection != null && localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack, streamIds);
            }
            if (peerConnection != null && localAudioTrack != null) {
                peerConnection.addTrack(localAudioTrack, streamIds);
            }
        } catch (Exception e) {
            Log.e(TAG, "Init peer connection failed", e);
        }
    }

    private void handleOfferInternal(String sdp) {
        try {
            runOnUiThread(() -> statusText.setText("收到对方请求，创建响应..."));
            
            if (localVideoTrack == null) createLocalTracks();
            if (peerConnection == null) initPeerConnectionForAnswer();

            peerConnection.setRemoteDescription(new SimpleSdpObserver(),
                    new SessionDescription(SessionDescription.Type.OFFER, sdp));
            remoteDescriptionSet = true;
            flushCandidateQueue();

            MediaConstraints answerConstraints = new MediaConstraints();
            if (isAudioCall) {
                answerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
            }
            // 🔧 修复：确保 answer 中声明接收音频
            answerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
            peerConnection.createAnswer(new SimpleSdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    Log.d(TAG, "Answer created, sdp length=" + sessionDescription.description.length());
                    runOnUiThread(() -> statusText.setText("响应已创建，发送中..."));
                    final SessionDescription answer = sessionDescription;
                    peerConnection.setLocalDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(TAG, "Local description set, sending answer");
                            runOnUiThread(() -> statusText.setText("等待连接建立..."));
                            signalingClient.sendAnswer(answer.description);
                        }
                    }, answer);
                }
            }, answerConstraints);
        } catch (Exception e) {
            Log.e(TAG, "Handle offer failed", e);
            runOnUiThread(() -> statusText.setText("连接建立失败"));
        }
    }

    private void flushCandidateQueue() {
        for (IceCandidate candidate : candidateQueue) {
            peerConnection.addIceCandidate(candidate);
        }
        candidateQueue.clear();
    }

    // ==================== 本地音视频 ====================

    private void createLocalTracks() {
        try {
            if (localVideoTrack != null || localAudioTrack != null) {
                Log.d(TAG, "Local tracks already created, skipping");
                return;
            }
            
            Log.d(TAG, "Creating local tracks...");
            
            if (!isAudioCall) {
                VideoCapturer videoCapturer = createCameraCapturer();
                if (videoCapturer != null) {
                    Log.d(TAG, "Camera capturer created: " + videoCapturer.getClass().getSimpleName());
                    
                    VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
                    SurfaceTextureHelper surfaceTextureHelper =
                            SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                    videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
                    videoCapturer.startCapture(640, 480, 30);
                    Log.d(TAG, "Camera capture started");

                    localVideoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource);
                    localVideoTrack.setEnabled(true);
                    Log.d(TAG, "Local video track created, id=video_track");

                    if (localVideoContainer != null) {
                        Log.d(TAG, "localVideoContainer found, creating SurfaceViewRenderer");
                        
                        SurfaceViewRenderer localView = new SurfaceViewRenderer(this);
                        localView.init(eglBase.getEglBaseContext(), null);
                        localView.setMirror(true);
                        localView.setEnableHardwareScaler(true);
                        // 🔧 修复：本地预览使用 setZOrderOnTop(true) 确保显示在最上层
                        // setZOrderMediaOverlay 只在当前窗口内置顶，setZOrderOnTop 在整个窗口系统置顶
                        localView.setZOrderOnTop(true);
                        
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                        );
                        localView.setLayoutParams(params);
                        
                        localVideoTrack.addSink(localView);
                        localVideoContainer.removeAllViews();
                        localVideoContainer.addView(localView);
                        // 确保本地预览容器可见且背景透明
                        localVideoContainer.setVisibility(View.VISIBLE);
                        localVideoContainer.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        Log.d(TAG, "Local video view added to container successfully");
                    }
                } else {
                    Log.e(TAG, "Failed to create camera capturer!");
                }
            }

            AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
            localAudioTrack = peerConnectionFactory.createAudioTrack("audio_track", audioSource);
            localAudioTrack.setEnabled(true);
            Log.d(TAG, "Local audio track created");
            
        } catch (Exception e) {
            Log.e(TAG, "Create local tracks failed", e);
        }
    }

    private VideoCapturer createCameraCapturer() {
        CameraEnumerator enumerator = new Camera2Enumerator(this);
        String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        return null;
    }

    // ==================== UI 控制 ====================

    private void toggleMute() {
        isMuted = !isMuted;
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(!isMuted);
        }
        muteButton.setAlpha(isMuted ? 0.5f : 1.0f);
    }

    private void toggleVideo() {
        if (isAudioCall) return;
        isVideoEnabled = !isVideoEnabled;
        if (localVideoTrack != null) {
            localVideoTrack.setEnabled(isVideoEnabled);
        }
        videoButton.setAlpha(isVideoEnabled ? 1.0f : 0.5f);
        localVideoContainer.setVisibility(isVideoEnabled ? View.VISIBLE : View.GONE);
    }

    // ==================== 视频录制（MediaProjection 屏幕录制）====================

    /**
     * 🔧 修复：使用 MediaProjection API 录制屏幕，避免与 WebRTC 摄像头冲突
     */
    private void startRecording() {
        if (isRecording) {
            Log.d(TAG, "Already recording, skipping");
            return;
        }
        
        // 请求 MediaProjection 权限
        if (mediaProjection == null) {
            Log.d(TAG, "Requesting MediaProjection permission");
            mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
            return;
        }
        
        startScreenRecording();
    }
    
    private void startScreenRecording() {
        try {
            // 获取屏幕尺寸
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int density = metrics.densityDpi;
            
            // 降低分辨率以减小文件大小（录制一半分辨率）
            int recordWidth = width / 2;
            int recordHeight = height / 2;
            
            // 生成文件名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "call_" + appointmentId + "_" + timestamp + ".mp4";
            recordingFilePath = new File(outputDir, fileName).getAbsolutePath();
            
            Log.d(TAG, "Starting screen recording, file: " + recordingFilePath + 
                       ", size: " + recordWidth + "x" + recordHeight);
            
            // 创建 MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(recordingFilePath);
            mediaRecorder.setVideoSize(recordWidth, recordHeight);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoEncodingBitRate(1500000); // 1.5Mbps
            mediaRecorder.setVideoFrameRate(15);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            
            mediaRecorder.prepare();
            
            // 创建 VirtualDisplay
            Surface surface = mediaRecorder.getSurface();
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenRecording",
                recordWidth, recordHeight, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null
            );
            
            mediaRecorder.start();
            
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            
            // 显示录制指示器
            runOnUiThread(() -> {
                if (recordingIndicator != null) {
                    recordingIndicator.setVisibility(View.VISIBLE);
                }
                if (recordingDot != null) {
                    animateRecordingDot();
                }
                if (recordingText != null) {
                    recordingText.setText("通话录制中");
                }
            });
            
            // 通知对方开始录制
            sendRecordingNotification(true);
            
            // 启动定时检查
            startRecordingTimer();
            
            Toast.makeText(this, "通话已开始录制", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Start screen recording failed", e);
            Toast.makeText(this, "录制启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            cleanupRecordingResources();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                Log.d(TAG, "MediaProjection permission granted");
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                startScreenRecording();
            } else {
                Log.w(TAG, "MediaProjection permission denied");
                Toast.makeText(this, "需要屏幕录制权限才能保存通话记录", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void animateRecordingDot() {
        if (recordingDot == null) return;
        
        android.animation.ObjectAnimator fadeAnim = android.animation.ObjectAnimator.ofFloat(recordingDot, "alpha", 1f, 0.3f);
        fadeAnim.setDuration(500);
        fadeAnim.setRepeatCount(-1);
        fadeAnim.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        fadeAnim.start();
    }

    private void startRecordingTimer() {
        recordingTimerHandler = new Handler(Looper.getMainLooper());
        recordingTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording && recordingFilePath != null) {
                    File file = new File(recordingFilePath);
                    if (file.exists()) {
                        long sizeMB = file.length() / (1024 * 1024);
                        Log.d(TAG, "Recording file size: " + sizeMB + " MB");
                        
                        // 如果超过 90MB，停止并上传
                        if (sizeMB > 90) {
                            Log.w(TAG, "Recording file too large, stopping to upload");
                            stopAndUploadRecording();
                        } else {
                            recordingTimerHandler.postDelayed(this, 30000);
                        }
                    }
                }
            }
        };
        recordingTimerHandler.postDelayed(recordingTimerRunnable, 30000);
    }

    private void stopRecording() {
        if (!isRecording) return;
        
        try {
            isRecording = false;
            
            if (recordingTimerHandler != null && recordingTimerRunnable != null) {
                recordingTimerHandler.removeCallbacks(recordingTimerRunnable);
            }
            
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (Exception e) {
                    Log.w(TAG, "MediaRecorder stop failed: " + e.getMessage());
                }
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            if (virtualDisplay != null) {
                virtualDisplay.release();
                virtualDisplay = null;
            }
            
            Log.d(TAG, "Recording stopped, file: " + recordingFilePath);
            
            runOnUiThread(() -> {
                if (recordingIndicator != null) {
                    recordingIndicator.setVisibility(View.GONE);
                }
            });
            
            sendRecordingNotification(false);
            
        } catch (Exception e) {
            Log.e(TAG, "Stop recording failed", e);
        }
    }
    
    private void cleanupRecordingResources() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (virtualDisplay != null) {
                virtualDisplay.release();
                virtualDisplay = null;
            }
            if (mediaProjection != null) {
                mediaProjection.stop();
                mediaProjection = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Cleanup recording resources failed: " + e.getMessage());
        }
    }

    private void stopAndUploadRecording() {
        stopRecording();
        
        if (recordingFilePath != null) {
            File file = new File(recordingFilePath);
            if (file.exists() && file.length() > 0) {
                uploadRecordingFile(file);
            }
        }
    }

    private void uploadRecordingFile(File file) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Uploading recording file: " + file.getName() + ", size: " + file.length());
                
                String baseUrl = NetworkConfig.getBaseUrl();
                String uploadUrl = baseUrl + "/upload/chat-video";
                
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) 
                    new java.net.URL(uploadUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                
                String token = PreferenceStore.getInstance(this).getAuthToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                
                java.io.OutputStream out = conn.getOutputStream();
                StringBuilder sb = new StringBuilder();
                sb.append("--").append(boundary).append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
                sb.append("Content-Type: video/mp4\r\n\r\n");
                out.write(sb.toString().getBytes());
                
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                fis.close();
                
                sb.setLength(0);
                sb.append("\r\n--").append(boundary).append("--\r\n");
                out.write(sb.toString().getBytes());
                out.flush();
                out.close();
                
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Upload response code: " + responseCode);
                
                if (responseCode == 200 || responseCode == 201) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    Log.d(TAG, "Upload response: " + response.toString());
                    
                    JSONObject json = new JSONObject(response.toString());
                    // 🔧 修复：服务器返回的字段名是 "data" 不是 "url"
                    String mediaUrl = json.optString("data", "");
                    
                    if (!mediaUrl.isEmpty()) {
                        // 如果返回的是相对路径，拼接完整 URL
                        if (mediaUrl.startsWith("/")) {
                            String serverBaseUrl = NetworkConfig.getBaseUrl();
                            // 去掉 baseUrl 末尾的 /api，因为上传路径已经包含 /uploads
                            serverBaseUrl = serverBaseUrl.replaceAll("/api$", "");
                            mediaUrl = serverBaseUrl + mediaUrl;
                        }
                        saveVideoMessage(mediaUrl);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "录制已保存", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.w(TAG, "Upload response has no 'data' field: " + response.toString());
                        runOnUiThread(() -> {
                            Toast.makeText(this, "录制上传失败：服务器未返回文件地址", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Upload failed, response code: " + responseCode);
                }
                
                conn.disconnect();
                
            } catch (Exception e) {
                Log.e(TAG, "Upload recording failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "上传录制失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveVideoMessage(String mediaUrl) {
        try {
            if (appointmentId == null || appointmentId <= 0) return;
            
            ChatStore chatStore = new ChatStore(this);
            ChatMessageRecord record = new ChatMessageRecord(
                false, "SYSTEM", "[视频录制] 通话已录制", Uri.parse(mediaUrl)
            );
            chatStore.saveMessage(String.valueOf(appointmentId), record);
            
            Log.d(TAG, "Video recording saved to chat message");
            
        } catch (Exception e) {
            Log.e(TAG, "Save video message failed", e);
        }
    }

    private void sendRecordingNotification(boolean started) {
        try {
            if (signalingClient != null) {
                signalingClient.sendRecordingNotification(started);
                Log.d(TAG, "Sent recording notification: " + (started ? "started" : "stopped"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Send recording notification failed", e);
        }
    }

    // ==================== 结束通话 ====================

    private boolean isEndingCall = false;

    public void endCall() {
        if (isEndingCall) {
            Log.d(TAG, "endCall: already ending, skip");
            return;
        }
        isEndingCall = true;
        Log.d(TAG, "endCall: cleaning up...");
        try {
            // 停止视频录制并上传
            if (isRecording) {
                stopAndUploadRecording();
            }
            
            if (signalingClient != null) {
                try {
                    signalingClient.sendEnd();
                } catch (Exception e) {
                    Log.w(TAG, "sendEnd failed: " + e.getMessage());
                }
                try {
                    signalingClient.disconnect();
                } catch (Exception e) {
                    Log.w(TAG, "disconnect failed: " + e.getMessage());
                }
                signalingClient = null;
            }

            if (peerConnection != null) {
                try {
                    peerConnection.close();
                } catch (Exception e) {
                    Log.w(TAG, "peerConnection.close failed: " + e.getMessage());
                }
                peerConnection = null;
            }

            if (localVideoTrack != null) {
                try {
                    localVideoTrack.dispose();
                } catch (Exception e) {
                    Log.w(TAG, "localVideoTrack.dispose failed: " + e.getMessage());
                }
                localVideoTrack = null;
            }
            if (remoteVideoTrack != null) {
                try {
                    remoteVideoTrack.dispose();
                } catch (Exception e) {
                    Log.w(TAG, "remoteVideoTrack.dispose failed: " + e.getMessage());
                }
                remoteVideoTrack = null;
            }
            if (localAudioTrack != null) {
                try {
                    localAudioTrack.dispose();
                } catch (Exception e) {
                    Log.w(TAG, "localAudioTrack.dispose failed: " + e.getMessage());
                }
                localAudioTrack = null;
            }

            isInCall = false;
            pendingOffer = null;
            candidateQueue.clear();
            remoteDescriptionSet = false;
            // 🔧 修复：先保存通话记录，再重置 callStartTimeMs
            saveCallRecordToChat();
            callStartTimeMs = 0;

            Log.d(TAG, "Sending VIDEO_CALL_FINISHED broadcast");
            Intent done = new Intent(ACTION_VIDEO_CALL_FINISHED);
            done.setPackage(getPackageName());
            sendBroadcast(done);
            Log.d(TAG, "Broadcast sent, finishing activity");
        } catch (Exception e) {
            Log.e(TAG, "endCall error", e);
        }

        finish();
    }

    private void saveCallRecordToChat() {
        try {
            if (appointmentId == null || appointmentId <= 0) {
                Log.w(TAG, "Cannot save call record: invalid appointmentId");
                return;
            }
            
            long durationMs = 0;
            if (callStartTimeMs > 0) {
                durationMs = System.currentTimeMillis() - callStartTimeMs;
            }
            long durationSec = durationMs / 1000;
            
            String durationStr;
            if (durationSec < 60) {
                durationStr = durationSec + "秒";
            } else if (durationSec < 3600) {
                durationStr = (durationSec / 60) + "分" + (durationSec % 60) + "秒";
            } else {
                durationStr = (durationSec / 3600) + "时" + ((durationSec % 3600) / 60) + "分";
            }
            
            // 🔧 修复：使用 CALL_ENDED 格式，让 chat.html 的 renderMessage 能正确渲染
            String callTypeCode = isAudioCall ? "audio" : "video";
            String content;
            if (durationSec > 0) {
                content = String.format("CALL_ENDED:%s:%d:%s", callTypeCode, durationSec, durationStr);
            } else {
                content = String.format("CALL_ENDED:%s:0:已取消", callTypeCode);
            }
            
            // 1. 保存到本地存储
            ChatStore chatStore = new ChatStore(this);
            ChatMessageRecord record = new ChatMessageRecord(
                    false,
                    "SYSTEM",
                    content,
                    null
            );
            chatStore.saveMessage(String.valueOf(appointmentId), record);
            Log.d(TAG, "Call record saved to local chat: " + content);
            
            // 2. 🔧 修复：同步发送到服务器，让 Web 端也能看到通话记录
            sendCallRecordToServer(content);
            
        } catch (Exception e) {
            Log.e(TAG, "saveCallRecordToChat failed", e);
        }
    }

    /**
     * 🔧 新增：将通话记录发送到服务器，让 Web 端咨询师也能看到
     */
    private void sendCallRecordToServer(String content) {
        new Thread(() -> {
            try {
                String baseUrl = NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/messages");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + PreferenceStore.getInstance(this).getAuthToken());
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                // 构建请求体
                JSONObject body = new JSONObject();
                body.put("appointmentId", appointmentId);
                body.put("senderUserId", currentUserId != null ? currentUserId : 0);
                body.put("receiverUserId", targetUserId != null ? targetUserId : 0);
                body.put("messageType", "SYSTEM");
                body.put("content", content);
                body.put("isFromConsultant", false);
                
                java.io.OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 201) {
                    Log.d(TAG, "Call record sent to server successfully");
                } else {
                    Log.w(TAG, "Failed to send call record to server, code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "sendCallRecordToServer failed", e);
            }
        }).start();
    }

    // ==================== 生命周期 ====================

    @Override
    public void onBackPressed() {
        if (isInCall) {
            showExitConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 🔧 修复：拦截物理返回键和系统返回手势
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isInCall) {
            showExitConfirmDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出视频咨询")
                .setMessage("确定退出视频咨询吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    endCall();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopAndUploadRecording();
        }
        cleanupRecordingResources();
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (signalingClient != null) {
            signalingClient.disconnect();
            signalingClient = null;
        }
        if (eglBase != null) {
            eglBase.release();
            eglBase = null;
        }
    }

    // ==================== 辅助类 ====================

    private static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {}
        @Override
        public void onSetSuccess() {}
        @Override
        public void onCreateFailure(String s) {
            Log.e("SdpObserver", "Create failure: " + s);
        }
        @Override
        public void onSetFailure(String s) {
            Log.e("SdpObserver", "Set failure: " + s);
        }
    }

    private static class PeerConnectionObserver implements PeerConnection.Observer {
        @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
        @Override public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}
        @Override public void onIceConnectionReceivingChange(boolean b) {}
        @Override public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
        @Override public void onIceCandidate(IceCandidate iceCandidate) {}
        @Override public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
        @Override public void onAddStream(MediaStream mediaStream) {}
        @Override public void onRemoveStream(MediaStream mediaStream) {}
        @Override public void onDataChannel(DataChannel dataChannel) {}
        @Override public void onRenegotiationNeeded() {}
        @Override public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
        public void onConnectionChange(PeerConnection.PeerConnectionState newState) {}
    }
}