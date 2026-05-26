package com.videocall.android;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.webrtc.*;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SignalingClient.SignalingListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvStatus, tvLogs, tvError, tvErrorDetail;
    private Button btnCall, btnEnd;
    private LinearLayout errorContainer;
    private SurfaceViewRenderer localView, remoteView;

    private SignalingClient signalingClient;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private VideoTrack remoteVideoTrack;
    private boolean isInCall = false;

    // 统一的 EglBase（核心修复）
    private EglBase eglBase;
    
    private String pendingOffer = null;
    private List<IceCandidate> candidateQueue = new ArrayList<>();
    private boolean remoteDescriptionSet = false;

    private final String serverUrl = "ws://192.168.175.206:8080/signaling";

    private final String[] requiredPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private String fullErrorText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        tvLogs = findViewById(R.id.tvLogs);
        tvError = findViewById(R.id.tvError);
        tvErrorDetail = findViewById(R.id.tvErrorDetail);
        errorContainer = findViewById(R.id.errorContainer);
        btnCall = findViewById(R.id.btnCall);
        btnEnd = findViewById(R.id.btnEnd);
        localView = findViewById(R.id.localView);
        remoteView = findViewById(R.id.remoteView);

        tvLogs.setMovementMethod(new ScrollingMovementMethod());
        addLog("应用启动");
        
        // 给日志添加点击复制功能
        tvLogs.setOnClickListener(v -> {
            if (tvLogs.getText().length() > 0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("VideoCall Logs", tvLogs.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "日志已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

        errorContainer.setOnClickListener(v -> {
            if (!fullErrorText.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Error Log", fullErrorText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "错误信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

        setupButtons();
        initializeWebRTC();
        checkPermissions();
    }

    public void addLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String log = "[" + time + "] " + message + "\n";
        Log.d(TAG, message);
        runOnUiThread(() -> {
            tvLogs.append(log);
            tvLogs.scrollTo(0, tvLogs.getHeight());
        });
    }

    private void showError(String error) {
        showError(error, null);
    }

    private void showError(String error, Throwable throwable) {
        Log.e(TAG, error, throwable);
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("错误: ").append(error).append("\n");
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                sb.append(sw.toString());
            }
            fullErrorText = sb.toString();
            
            tvError.setText(error);
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                tvErrorDetail.setText(sw.toString());
            } else {
                tvErrorDetail.setText("");
            }
            errorContainer.setVisibility(View.VISIBLE);
            updateStatus("错误");
        });
    }

    private void clearError() {
        runOnUiThread(() -> {
            errorContainer.setVisibility(View.GONE);
            tvError.setText("");
            tvErrorDetail.setText("");
        });
    }

    private void setupButtons() {
        btnCall.setOnClickListener(v -> {
            try {
                clearError();
                startCall();
            } catch (Exception e) {
                showError("开始通话失败", e);
            }
        });
        btnEnd.setOnClickListener(v -> {
            try {
                clearError();
                endCall();
            } catch (Exception e) {
                showError("结束通话失败", e);
            }
        });
    }

    private void initializeWebRTC() {
        try {
            addLog("初始化 WebRTC...");
            PeerConnectionFactory.InitializationOptions options =
                    PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
            PeerConnectionFactory.initialize(options);

            // 统一的 EglBase（核心修复）
            eglBase = EglBase.create();
            EglBase.Context eglContext = eglBase.getEglBaseContext();
            addLog("EglBase 初始化完成，统一上下文: " + eglContext);

            PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
            builder.setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglContext, true, true));
            builder.setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglContext));

            peerConnectionFactory = builder.createPeerConnectionFactory();

            // 初始化视频渲染器，使用统一的 EglBase
            localView.init(eglContext, null);
            localView.setMirror(true);
            localView.setEnableHardwareScaler(true); // 启用硬件缩放
            localView.setEnabled(true);
            addLog("本地视频视图初始化完成");

            remoteView.init(eglContext, null);
            remoteView.setMirror(false); // 远程不需要镜像
            remoteView.setEnableHardwareScaler(true);
            remoteView.setEnabled(true);
            addLog("远程视频视图初始化完成");
        } catch (Exception e) {
            showError("WebRTC 初始化失败", e);
        }
    }

    private void checkPermissions() {
        try {
            List<String> permissionsNeeded = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }

            if (!permissionsNeeded.isEmpty()) {
                addLog("请求权限: " + permissionsNeeded);
                ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            } else {
                connectToSignalingServer();
            }
        } catch (Exception e) {
            showError("检查权限失败", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                addLog("权限已获取");
                connectToSignalingServer();
            } else {
                showError("权限被拒绝", null);
                Toast.makeText(this, "需要相机和麦克风权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void connectToSignalingServer() {
        try {
            addLog("连接信令服务器: " + serverUrl);
            signalingClient = new SignalingClient(this);
            signalingClient.connect(serverUrl);
        } catch (Exception e) {
            showError("连接信令服务器失败", e);
        }
    }

    private void flushCandidateQueue() {
        addLog("【flushCandidateQueue】开始刷新队列，数量: " + candidateQueue.size());
        for (int i = 0; i < candidateQueue.size(); i++) {
            IceCandidate candidate = candidateQueue.get(i);
            String logStr = candidate.sdp.length() > 50 ? candidate.sdp.substring(0, 50) + "..." : candidate.sdp;
            addLog("【flushCandidateQueue】处理队列中的 candidate " + (i+1) + "/" + candidateQueue.size() + ": " + logStr);
            try {
                peerConnection.addIceCandidate(candidate);
                addLog("【flushCandidateQueue】队列 candidate " + (i+1) + " 添加成功");
            } catch (Exception e) {
                addLog("【flushCandidateQueue】队列 candidate " + (i+1) + " 添加失败: " + e.getMessage());
            }
        }
        candidateQueue.clear();
        addLog("【flushCandidateQueue】队列已清空");
    }

    private void startCall() {
        try {
            addLog("开始通话...");
            candidateQueue.clear();
            remoteDescriptionSet = false;
            
            createLocalTracks();

            MediaConstraints constraints = new MediaConstraints();
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));

            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
            // STUN
            iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
            // TURN（关键）
            iceServers.add(
                PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer()
            );

            PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(iceServers);
            config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
            config.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;

            peerConnection = peerConnectionFactory.createPeerConnection(config,
                    new CustomPeerConnectionObserver("PCObserver") {
                        @Override
                        public void onIceCandidate(IceCandidate iceCandidate) {
                            super.onIceCandidate(iceCandidate);
                            try {
                                JSONObject candidateJson = new JSONObject();
                                candidateJson.put("candidate", iceCandidate.sdp);
                                candidateJson.put("sdpMid", iceCandidate.sdpMid);
                                candidateJson.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                                
                                String candidateStr = iceCandidate.sdp.length() > 50 ? iceCandidate.sdp.substring(0, 50) + "..." : iceCandidate.sdp;
                                addLog("发送 ICE candidate: " + candidateStr);
                                
                                signalingClient.sendIceCandidate(candidateJson);
                            } catch (Exception e) {
                                showError("发送 ICE candidate 失败", e);
                            }
                        }

                        @Override
                        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                            super.onAddTrack(rtpReceiver, mediaStreams);
                            addLog("【onAddTrack】收到远程流: " + (rtpReceiver.track() != null ? rtpReceiver.track().kind() : "null"));
                            if (rtpReceiver.track() instanceof VideoTrack) {
                                remoteVideoTrack = (VideoTrack) rtpReceiver.track();
                                runOnUiThread(() -> {
                                    addLog("【渲染】远程视频添加到 remoteView");
                                    remoteVideoTrack.addSink(remoteView);
                                });
                            }
                        }
                    });

            // 统一的 streamId（核心修复）
            List<String> streamIds = new ArrayList<>();
            streamIds.add("stream");
            addLog("【addTrack】准备添加本地视频和音频");

            if (peerConnection != null && localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack, streamIds);
                addLog("【addTrack】本地视频添加成功");
            }

            if (peerConnection != null && localAudioTrack != null) {
                peerConnection.addTrack(localAudioTrack, streamIds);
                addLog("【addTrack】本地音频添加成功");
            }

            peerConnection.createOffer(new CustomSdpObserver("createOffer") {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    addLog("创建 offer 成功");
                    peerConnection.setLocalDescription(new CustomSdpObserver("setLocal"), sessionDescription);
                    try {
                        addLog("发送 call 信号");
                        signalingClient.sendCall();
                        
                        addLog("发送 offer (使用 sdp 字段)");
                        signalingClient.sendOffer(sessionDescription.description);
                    } catch (Exception e) {
                        showError("发送 offer 失败", e);
                    }
                }

                @Override
                public void onCreateFailure(String s) {
                    showError("创建 offer 失败: " + s, null);
                }

                @Override
                public void onSetFailure(String s) {
                    showError("设置 local 描述失败: " + s, null);
                }
            }, constraints);

            isInCall = true;
            updateStatus("通话中");
            updateButtons();
        } catch (Exception e) {
            showError("开始通话异常", e);
        }
    }

    private void createLocalTracks() {
        try {
            addLog("【createLocalTracks】开始创建本地视频和音频");
            
            VideoCapturer videoCapturer = createCameraCapturer();
            if (videoCapturer == null) {
                showError("无法找到相机设备", null);
                return;
            }

            VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
            addLog("【createLocalTracks】VideoSource 创建成功");
            
            // 使用统一的 EglBase 创建 SurfaceTextureHelper（核心修复）
            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            addLog("【createLocalTracks】SurfaceTextureHelper 创建成功，使用统一 EglBase");

            videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
            addLog("【createLocalTracks】videoCapturer.initialize 调用成功");

            localVideoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource);
            addLog("【createLocalTracks】VideoTrack 创建成功");

            // 启动摄像头，加上详细日志
            addLog("【createLocalTracks】准备调用 videoCapturer.startCapture(640, 480, 30)");
            videoCapturer.startCapture(640, 480, 30);
            addLog("【createLocalTracks】videoCapturer.startCapture 调用成功");

            if (localVideoTrack != null) {
                localVideoTrack.addSink(localView);
                addLog("【createLocalTracks】本地视频添加到 localView 成功");
            }

            // 创建音频
            AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
            localAudioTrack = peerConnectionFactory.createAudioTrack("audio_track", audioSource);
            addLog("【createLocalTracks】AudioTrack 创建成功");

            addLog("【createLocalTracks】本地音视频创建完全部成功！");
        } catch (Exception e) {
            showError("创建本地音视频失败", e);
        }
    }

    private VideoCapturer createCameraCapturer() {
        CameraEnumerator enumerator = new Camera2Enumerator(this);
        String[] deviceNames = enumerator.getDeviceNames();

        addLog("找到相机数量: " + deviceNames.length);
        for (String deviceName : deviceNames) {
            addLog("相机设备: " + deviceName + ", 是否前置: " + enumerator.isFrontFacing(deviceName));
        }

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                addLog("使用前置相机: " + deviceName);
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                addLog("使用后置相机: " + deviceName);
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        return null;
    }

    private void endCall() {
        try {
            addLog("结束通话...");

            if (peerConnection != null) {
                peerConnection.close();
                peerConnection = null;
            }

            if (localVideoTrack != null) {
                localVideoTrack.removeSink(localView);
                localVideoTrack = null;
            }

            if (remoteVideoTrack != null) {
                remoteVideoTrack.removeSink(remoteView);
                remoteVideoTrack = null;
            }

            if (localAudioTrack != null) {
                localAudioTrack = null;
            }

            if (signalingClient != null) {
                signalingClient.sendEnd();
            }

            pendingOffer = null;
            candidateQueue.clear();
            remoteDescriptionSet = false;
            
            isInCall = false;
            updateStatus("已连接");
            updateButtons();
        } catch (Exception e) {
            showError("结束通话异常", e);
        }
    }

    private void updateStatus(String status) {
        runOnUiThread(() -> tvStatus.setText("状态: " + status));
    }

    private void updateButtons() {
        runOnUiThread(() -> {
            btnCall.setEnabled(!isInCall);
            btnEnd.setEnabled(isInCall);
        });
    }

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            addLog("已连接到信令服务器");
            updateStatus("已连接");
            Toast.makeText(this, "已连接到服务器", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            addLog("与信令服务器断开连接");
            updateStatus("未连接");
            Toast.makeText(this, "与服务器断开连接", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCall(String fromId) {
        addLog("【处理call】收到来电请求 from: " + fromId);
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("来电")
                    .setMessage("有来电，是否接听？")
                    .setPositiveButton("接听", (dialog, which) -> {
                        try {
                            addLog("【处理call】用户选择接听来电");
                            isInCall = true;
                            updateStatus("通话中");
                            updateButtons();
                            
                            if (pendingOffer != null) {
                                addLog("【处理call】收到 pending offer，开始处理");
                                handleOfferInternal(pendingOffer);
                                pendingOffer = null;
                            } else {
                                addLog("【处理call】等待收到 offer...");
                                createLocalTracks();
                                initPeerConnectionForAnswer();
                            }
                        } catch (Exception e) {
                            showError("接听来电失败", e);
                        }
                    })
                    .setNegativeButton("拒绝", (dialog, which) -> {
                        addLog("【处理call】用户拒绝来电");
                        signalingClient.sendEnd();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void initPeerConnectionForAnswer() {
        try {
            MediaConstraints constraints = new MediaConstraints();
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));

            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
            // STUN
            iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
            // TURN（关键）
            iceServers.add(
                PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                    .setUsername("openrelayproject")
                    .setPassword("openrelayproject")
                    .createIceServer()
            );

            PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(iceServers);
            config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
            config.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;

            peerConnection = peerConnectionFactory.createPeerConnection(config,
                    new CustomPeerConnectionObserver("PCObserver") {
                        @Override
                        public void onIceCandidate(IceCandidate iceCandidate) {
                            super.onIceCandidate(iceCandidate);
                            try {
                                JSONObject candidateJson = new JSONObject();
                                candidateJson.put("candidate", iceCandidate.sdp);
                                candidateJson.put("sdpMid", iceCandidate.sdpMid);
                                candidateJson.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                                
                                String candidateStr = iceCandidate.sdp.length() > 50 ? iceCandidate.sdp.substring(0, 50) + "..." : iceCandidate.sdp;
                                addLog("发送 ICE candidate: " + candidateStr);
                                
                                signalingClient.sendIceCandidate(candidateJson);
                            } catch (Exception e) {
                                showError("发送 ICE candidate 失败", e);
                            }
                        }

                        @Override
                        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                            super.onAddTrack(rtpReceiver, mediaStreams);
                            addLog("【onAddTrack】收到远程流: " + (rtpReceiver.track() != null ? rtpReceiver.track().kind() : "null"));
                            if (rtpReceiver.track() instanceof VideoTrack) {
                                remoteVideoTrack = (VideoTrack) rtpReceiver.track();
                                runOnUiThread(() -> {
                                    addLog("【渲染】远程视频添加到 remoteView");
                                    remoteVideoTrack.addSink(remoteView);
                                });
                            }
                        }
                    });

            // 统一的 streamId
            List<String> streamIds = new ArrayList<>();
            streamIds.add("stream");
            addLog("【addTrack】准备添加本地视频和音频（接听模式）");

            if (peerConnection != null && localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack, streamIds);
                addLog("【addTrack】本地视频添加成功（接听模式）");
            }

            if (peerConnection != null && localAudioTrack != null) {
                peerConnection.addTrack(localAudioTrack, streamIds);
                addLog("【addTrack】本地音频添加成功（接听模式）");
            }
        } catch (Exception e) {
            showError("初始化 PeerConnection 失败", e);
        }
    }

    @Override
    public void onOffer(String sdp, String fromId) {
        addLog("【处理offer】收到 offer from: " + fromId + ", sdp 长度: " + (sdp != null ? sdp.length() : 0));
        
        if (isInCall && peerConnection != null) {
            addLog("【处理offer】已经在通话中，直接处理");
            handleOfferInternal(sdp);
        } else if (!isInCall) {
            addLog("【处理offer】还未接听，保存 pending offer");
            pendingOffer = sdp;
        }
    }

    private void handleOfferInternal(String sdp) {
        try {
            if (localVideoTrack == null) {
                addLog("本地视频流未准备，先创建");
                createLocalTracks();
            }

            if (peerConnection == null) {
                addLog("PeerConnection 未初始化，先初始化");
                initPeerConnectionForAnswer();
            }

            addLog("设置远程描述 (offer)");
            peerConnection.setRemoteDescription(new CustomSdpObserver("setRemote") {
                @Override
                public void onSetSuccess() {
                    addLog("设置远程描述成功");
                    remoteDescriptionSet = true;
                    addLog("【handleOffer】remoteDescriptionSet 设为 true");
                    flushCandidateQueue();
                    
                    addLog("创建 answer");
                    peerConnection.createAnswer(new CustomSdpObserver("createAnswer") {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            addLog("创建 answer 成功");
                            peerConnection.setLocalDescription(new CustomSdpObserver("setLocalAnswer"), sessionDescription);
                            try {
                                addLog("发送 answer (使用 sdp 字段)");
                                signalingClient.sendAnswer(sessionDescription.description);
                            } catch (Exception e) {
                                showError("发送 answer 失败", e);
                            }
                        }

                        @Override
                        public void onCreateFailure(String s) {
                            showError("创建 answer 失败: " + s, null);
                        }

                        @Override
                        public void onSetFailure(String s) {
                            showError("设置本地描述 (answer) 失败: " + s, null);
                        }
                    }, new MediaConstraints());
                }

                @Override
                public void onSetFailure(String s) {
                    showError("设置远程描述 (offer) 失败: " + s, null);
                }
            }, new SessionDescription(SessionDescription.Type.OFFER, sdp));
        } catch (Exception e) {
            showError("处理 offer 失败", e);
        }
    }

    @Override
    public void onAnswer(String sdp, String fromId) {
        addLog("【处理answer】收到 answer from: " + fromId + ", sdp 长度: " + (sdp != null ? sdp.length() : 0));
        
        if (peerConnection != null) {
            try {
                addLog("【处理answer】开始设置远程描述");
                peerConnection.setRemoteDescription(new CustomSdpObserver("setRemoteAnswer") {
                    @Override
                    public void onSetSuccess() {
                        addLog("【处理answer】设置远程描述 (answer) 成功");
                        remoteDescriptionSet = true;
                        addLog("【handleAnswer】remoteDescriptionSet 设为 true");
                        flushCandidateQueue();
                    }
                    
                    @Override
                    public void onSetFailure(String s) {
                        showError("【处理answer】设置远程描述 (answer) 失败: " + s, null);
                    }
                }, new SessionDescription(SessionDescription.Type.ANSWER, sdp));
            } catch (Exception e) {
                showError("【处理answer】处理 answer 失败", e);
            }
        } else {
            addLog("【处理answer】PeerConnection 为 null，无法处理 answer");
        }
    }

    @Override
    public void onIceCandidate(String candidate, String fromId) {
        addLog("【处理candidate】收到 candidate from: " + fromId);
        
        if (peerConnection != null) {
            try {
                JSONObject json = null;
                try {
                    json = new JSONObject(candidate);
                    addLog("【处理candidate】解析为 JSON 对象成功");
                } catch (Exception e) {
                    addLog("【处理candidate】不是 JSON，当作纯字符串处理");
                }
                
                String candidateStr = "";
                String sdpMid = "";
                int sdpMLineIndex = 0;
                
                if (json != null) {
                    candidateStr = json.optString("candidate", "");
                    sdpMid = json.optString("sdpMid", "");
                    sdpMLineIndex = json.optInt("sdpMLineIndex", 0);
                    addLog("【处理candidate】解析后 candidateStr: " + (candidateStr.length() > 50 ? candidateStr.substring(0, 50) + "..." : candidateStr));
                    addLog("【处理candidate】解析后 sdpMid: " + sdpMid + ", sdpMLineIndex: " + sdpMLineIndex);
                }
                
                if (candidateStr.isEmpty()) {
                    addLog("【处理candidate】跳过空的 ICE candidate");
                    return;
                }
                
                IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidateStr);
                
                if (remoteDescriptionSet) {
                    addLog("【处理candidate】可以添加，remoteDescription 已设置");
                    peerConnection.addIceCandidate(iceCandidate);
                    String logStr = candidateStr.length() > 50 ? candidateStr.substring(0, 50) + "..." : candidateStr;
                    addLog("【处理candidate】添加 ICE candidate 成功: " + logStr);
                } else {
                    addLog("【处理candidate】remoteDescription 未设置，缓存");
                    candidateQueue.add(iceCandidate);
                    addLog("【处理candidate】已缓存，队列长度: " + candidateQueue.size());
                }
            } catch (Exception e) {
                showError("【处理candidate】添加 ICE candidate 失败", e);
            }
        }
    }

    @Override
    public void onEnd(String fromId) {
        addLog("【处理end】收到结束通话消息 from: " + fromId);
        runOnUiThread(() -> {
            if (isInCall) {
                Toast.makeText(this, "对方已结束通话", Toast.LENGTH_SHORT).show();
                endCall();
            }
        });
    }

    @Override
    public void onUserCount(int count) {
    }

    @Override
    public void onError(String error) {
        showError("信令错误: " + error, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            endCall();
            if (signalingClient != null) {
                signalingClient.disconnect();
            }
            if (localView != null) {
                localView.release();
            }
            if (remoteView != null) {
                remoteView.release();
            }
            if (eglBase != null) {
                eglBase.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "清理资源失败", e);
        }
    }

    private static class CustomSdpObserver implements SdpObserver {
        private final String tag;

        CustomSdpObserver(String tag) {
            this.tag = tag;
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.d(tag, "SdpObserver onCreateSuccess");
        }

        @Override
        public void onSetSuccess() {
            Log.d(tag, "SdpObserver onSetSuccess");
        }

        @Override
        public void onSetFailure(String s) {
            Log.e(tag, "SdpObserver onSetFailure: " + s);
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(tag, "SdpObserver onCreateFailure: " + s);
        }
    }

    private static class CustomPeerConnectionObserver implements PeerConnection.Observer {
        private final String tag;

        CustomPeerConnectionObserver(String tag) {
            this.tag = tag;
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(tag, "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(tag, "onIceConnectionChange: " + iceConnectionState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d(tag, "onIceConnectionReceivingChange: " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(tag, "onIceGatheringChange: " + iceGatheringState);
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(tag, "onIceCandidate: " + iceCandidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            Log.d(tag, "onIceCandidatesRemoved");
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d(tag, "onAddStream");
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d(tag, "onRemoveStream");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d(tag, "onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d(tag, "onRenegotiationNeeded");
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            Log.d(tag, "onAddTrack");
        }
    }
}
