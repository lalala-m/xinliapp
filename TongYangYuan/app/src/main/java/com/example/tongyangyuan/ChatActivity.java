package com.example.tongyangyuan;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.tongyangyuan.consult.Consultant;
import com.example.tongyangyuan.webview.WebAppInterface;
import com.example.tongyangyuan.VideoCallActivity;
import com.just.agentweb.AgentWeb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements WebAppInterface.MediaDelegate {

    public static final String KEY_CONSULTANT = "chat_consultant";
    public static final String KEY_APPOINTMENT_DATE = "chat_appointment_date";
    public static final String KEY_APPOINTMENT_SLOT = "chat_appointment_slot";
    public static final String KEY_APPOINTMENT_ID = "chat_appointment_id";
    public static final String KEY_CHILD_ID = "chat_child_id";
    public static final String KEY_CHILD_NAME = "chat_child_name";
    public static final String KEY_SERVER_ID = "chat_server_id";

    private AgentWeb mAgentWeb;
    private WebAppInterface webInterface;
    private Consultant consultant;
    private String appointmentDate;
    private String appointmentSlot;
    private String appointmentId;
    private String childId;
    private String childName;
    private long serverAppointmentId = -1;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private ActivityResultLauncher<Uri> captureImageLauncher;
    private ActivityResultLauncher<Uri> captureVideoLauncher;
    private ActivityResultLauncher<String> audioPermissionLauncher;

    private Uri pendingImageUri;
    private Uri pendingVideoUri;

    private MediaRecorder mediaRecorder;
    private File recordingFile;
    private MediaPlayer mediaPlayer;

    private boolean videoCallReceiverRegistered;
    private final BroadcastReceiver videoCallFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (webInterface != null) {
                webInterface.notifyVideoCallClosed();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        extractIntentData();
        setupWebView();
        registerLaunchers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!videoCallReceiverRegistered) {
            IntentFilter f = new IntentFilter(VideoCallActivity.ACTION_VIDEO_CALL_FINISHED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(videoCallFinishedReceiver, f, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(videoCallFinishedReceiver, f);
            }
            videoCallReceiverRegistered = true;
        }
    }

    @Override
    protected void onStop() {
        if (videoCallReceiverRegistered) {
            unregisterReceiver(videoCallFinishedReceiver);
            videoCallReceiverRegistered = false;
        }
        super.onStop();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            consultant = (Consultant) intent.getSerializableExtra(KEY_CONSULTANT);
            appointmentDate = intent.getStringExtra(KEY_APPOINTMENT_DATE);
            appointmentSlot = intent.getStringExtra(KEY_APPOINTMENT_SLOT);
            appointmentId = intent.getStringExtra(KEY_APPOINTMENT_ID);
            childId = intent.getStringExtra(KEY_CHILD_ID);
            childName = intent.getStringExtra(KEY_CHILD_NAME);
            serverAppointmentId = intent.getLongExtra(KEY_SERVER_ID, -1);
        }
    }

    private void setupWebView() {
        ViewGroup container = findViewById(R.id.container);

        // 提前创建 WebAppInterface，WebView 引用后续再补充
        // 原因：DOMContentLoaded 早于 onPageFinished，所以必须把接口预注入到 JS 上下文
        initWebInterface(null);

        // 创建 AgentWeb：先调用 .go(null) 让 AgentWeb 初始化 WebView，
        // 此时页面尚未开始解析，再注入 JS 接口，最后才 loadUrl
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(2, Color.TRANSPARENT)
                .createAgentWeb()
                .ready()
                .go(null);

        // 在 go(null) 之后、loadUrl 之前注入 JS 接口
        mAgentWeb.getJsInterfaceHolder().addJavaObject("Android", webInterface);
        Log.d("ChatActivity", "Android interface pre-injected before page load");

        // 获取 WebView 并设置配置
        WebView webView = mAgentWeb.getWebCreator().getWebView();
        configureWebView(webView);

        // 补充 WebView 引用到接口（dispatchJs / notifyMediaMessage 等方法需要）
        webInterface.setWebView(webView);

        // 向接口写入会话数据
        long cuid = consultant != null ? consultant.getUserId() : 0L;
        String cname = consultant != null ? consultant.getName() : "";
        webInterface.setChatSessionForWebView(
                appointmentId != null ? appointmentId : "",
                serverAppointmentId,
                cname,
                cuid
        );

        // 开始加载页面（接口已注入，DOMContentLoaded 时 window.Android 就已存在）
        mAgentWeb.getUrlLoader().loadUrl("file:///android_asset/chat.html");

        Log.d("ChatActivity", "setupWebView done, Android interface pre-injected before page load");
    }

    private void configureWebView(WebView webView) {
        // WebSettings
        android.webkit.WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // 禁用硬件加速（解决模拟器 GPU 不足导致的 shader 报错）
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // WebChromeClient：转发 JS console 到 logcat
        webView.setWebChromeClient(new WebChromeClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceId) {
                Log.d("ChatWebChrome", "[JS " + sourceId + ":" + lineNumber + "] " + message);
                super.onConsoleMessage(message, lineNumber, sourceId);
            }
        });

        // WebViewClient
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                Log.d("ChatActivity", "onPageStarted: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("ChatActivity", "onPageFinished: " + url);
                // 接口已在 setupWebView 中通过 AgentWeb.jsInterface 预注入，此处不再重复注入
                // 仅通知 JS 页面已就绪
                if (webInterface != null) {
                    webInterface.setWebView(view);
                }
                view.post(() -> {
                    String initScript = "(function() { " +
                            "console.log('[ChatActivity] onPageFinished - notifying JS'); " +
                            "if (typeof window.onNativeReady === 'function') { " +
                            "    window.onNativeReady(); " +
                            "} else { " +
                            "    console.error('[ChatActivity] onNativeReady not found!'); " +
                            "} " +
                            "})();";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        view.evaluateJavascript(initScript, value -> {
                            Log.d("ChatActivity", "onNativeReady called, result: " + value);
                        });
                    } else {
                        view.loadUrl("javascript:" + initScript);
                    }
                });
            }
        });
    }

    private void initWebInterface(WebView webView) {
        if (webInterface != null) return; // 已初始化
        webInterface = new WebAppInterface(this, webView);
        webInterface.setMediaDelegate(this);
        // 注意：chat session 数据（setChatSessionForWebView）由调用方在注入后补充写入
        // 因为 webView 可能为 null（预注入场景），Session 数据在 setupWebView 末尾统一写入
    }

    private void registerLaunchers() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                takePersistablePermission(uri);
                handleImageResult(uri);
            }
        });

        pickVideoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                takePersistablePermission(uri);
                handleVideoResult(uri);
            }
        });

        captureImageLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && pendingImageUri != null) {
                handleImageResult(pendingImageUri);
            }
        });

        captureVideoLauncher = registerForActivityResult(new ActivityResultContracts.CaptureVideo(), result -> {
            if (result && pendingVideoUri != null) {
                handleVideoResult(pendingVideoUri);
            }
        });

        audioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        startVoiceRecordingInternal();
                    } else {
                        showToast("录音权限被拒绝");
                        if (webInterface != null) {
                            webInterface.notifyVoiceRecordState(false);
                        }
                    }
                }
        );
    }

    private void initStomp() {
        // Removed Stomp client connection
    }

    @Override
    protected void onDestroy() {
        if (webInterface != null) {
            // Cancel voice recording callback to prevent toast on exit
            webInterface.setVoiceRecordCallbackEnabled(false);
        }
        stopVoiceRecorderInternal(false); // Don't send message on destroy
        releaseMediaPlayer();
        super.onDestroy();
    }

    // MediaDelegate Implementation

    @Override
    public void pickImage() {
        pickImageLauncher.launch("image/*");
    }

    @Override
    public void captureImage() {
        pendingImageUri = createImageUri();
        if (pendingImageUri == null) {
            showToast("无法创建照片文件");
            return;
        }
        captureImageLauncher.launch(pendingImageUri);
    }

    @Override
    public void pickVideo() {
        pickVideoLauncher.launch("video/*");
    }

    @Override
    public void captureVideo() {
        pendingVideoUri = createVideoUri();
        if (pendingVideoUri == null) {
            showToast("无法创建视频文件");
            return;
        }
        captureVideoLauncher.launch(pendingVideoUri);
    }

    @Override
    public void startVoiceRecord() {
        if (mediaRecorder != null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }
        startVoiceRecordingInternal();
    }

    @Override
    public void stopVoiceRecord() {
        stopVoiceRecorderInternal(true);
    }
    
    @Override
    public void cancelVoiceRecord() {
        stopVoiceRecorderInternal(false);
    }

    @Override
    public void playMedia(String type, String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            showToast("媒体内容不存在");
            return;
        }
        Uri uri = Uri.parse(uriString);
        if ("audio".equalsIgnoreCase(type)) {
            playAudio(uri);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(intent, "播放视频"));
            } catch (Exception e) {
                showToast("无法播放该视频");
            }
        }
    }

    // Helpers

    private void takePersistablePermission(@NonNull Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException ignore) {
                // Ignore if not supported
            }
        }
    }

    private Uri createImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TongYangYuan");
        ContentResolver resolver = getContentResolver();
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Uri createVideoUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_" + timeStamp + ".mp4");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TongYangYuan");
        ContentResolver resolver = getContentResolver();
        return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void handleImageResult(@NonNull Uri uri) {
        String base64 = convertImageToBase64(uri);
        if (base64 == null) {
            showToast("图片读取失败");
            return;
        }
        String dataUrl = "data:image/jpeg;base64," + base64;
        if (webInterface != null) {
            webInterface.notifyMediaMessage("image", "已发送图片", dataUrl);
        }
    }

    private void handleVideoResult(@NonNull Uri uri) {
        if (webInterface != null) {
            webInterface.notifyMediaMessage("video", "已发送视频", uri.toString());
        }
    }

    private void handleAudioResult(@NonNull File audioFile) {
        Log.d("ChatActivity", "[VOICE] handleAudioResult 开始, 文件路径: " + audioFile.getAbsolutePath() + ", 存在=" + audioFile.exists() + ", 大小=" + (audioFile.exists() ? audioFile.length() : 0));
        
        // 获取音频时长
        String content = "语音消息";
        long durationMs = 0;
        try {
            android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
            retriever.setDataSource(audioFile.getAbsolutePath());
            String durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            
            if (durationStr != null) {
                durationMs = Long.parseLong(durationStr);
                // 如果录音太短（<0.5秒），取消发送
                if (durationMs < 500) {
                    Log.d("ChatActivity", "[VOICE] 录音太短: " + durationMs + "ms，取消发送");
                    showToast("录音时间过短，已取消");
                    audioFile.delete();
                    return;
                }
                // 格式化时长为 mm:ss
                long seconds = durationMs / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                content = String.format(Locale.getDefault(), "语音消息 %02d:%02d", minutes, seconds);
                Log.d("ChatActivity", "[VOICE] 录音时长: " + content);
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "[VOICE] 获取音频时长失败", e);
        }
        
        // 上传音频文件到服务器
        final String finalContent = content;
        Log.d("ChatActivity", "[VOICE] 开始上传音频文件...");
        new Thread(() -> {
            try {
                String serverUrl = uploadMediaFile(audioFile, "audio/m4a");
                Log.d("ChatActivity", "[VOICE] 上传结果: " + (serverUrl != null ? serverUrl : "失败"));
                
                if (serverUrl != null) {
                    runOnUiThread(() -> {
                        Log.d("ChatActivity", "[VOICE] 调用 notifyMediaMessage, serverUrl=" + serverUrl);
                        if (webInterface != null) {
                            webInterface.notifyMediaMessage("audio", finalContent, serverUrl);
                            Log.d("ChatActivity", "[VOICE] notifyMediaMessage 调用成功");
                        } else {
                            Log.e("ChatActivity", "[VOICE] webInterface is null!");
                        }
                    });
                } else {
                    // 上传失败，使用本地 URI 作为后备
                    Uri localUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            audioFile
                    );
                    Log.d("ChatActivity", "[VOICE] 上传失败，使用本地 URI: " + localUri);
                    runOnUiThread(() -> {
                        if (webInterface != null) {
                            webInterface.notifyMediaMessage("audio", finalContent, localUri.toString());
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ChatActivity", "[VOICE] 上传音频文件失败", e);
                Uri localUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        audioFile
                );
                runOnUiThread(() -> {
                    if (webInterface != null) {
                        webInterface.notifyMediaMessage("audio", finalContent, localUri.toString());
                    }
                });
            }
        }).start();
        Log.d("ChatActivity", "[VOICE] handleAudioResult 完成");
    }
    
    /**
     * 上传媒体文件到服务器
     * @param file 要上传的文件
     * @param mimeType 文件的MIME类型
     * @return 服务器上的URL，失败返回null
     */
    private String uploadMediaFile(File file, String mimeType) {
        if (!file.exists()) {
            Log.e("ChatActivity", "上传文件不存在: " + file.getAbsolutePath());
            return null;
        }
        
        try {
            String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
            java.net.URL url = new java.net.URL(baseUrl + "/upload/audio");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            String token = webInterface != null ? webInterface.getAuthToken() : "";
            if (!android.text.TextUtils.isEmpty(token)) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            // 设置 multipart form data
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            java.io.OutputStream os = conn.getOutputStream();
            String fileName = file.getName();
            
            // 写入文件数据
            StringBuilder sb = new StringBuilder();
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
            sb.append("Content-Type: ").append(mimeType).append("\r\n\r\n");
            os.write(sb.toString().getBytes("UTF-8"));
            
            // 写入文件内容
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            fis.close();
            
            sb = new StringBuilder();
            sb.append("\r\n--").append(boundary).append("--\r\n");
            os.write(sb.toString().getBytes("UTF-8"));
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // 解析响应：{code: 200, data: {url: "..."}}
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                if (jsonResponse.optInt("code") == 200 && jsonResponse.has("data")) {
                    org.json.JSONObject data = jsonResponse.getJSONObject("data");
                    String fileUrl = data.optString("url", "");
                    if (!android.text.TextUtils.isEmpty(fileUrl)) {
                        // 确保 URL 是完整的
                        if (fileUrl.startsWith("http")) {
                            return fileUrl;
                        } else {
                            return baseUrl + fileUrl;
                        }
                    }
                }
            }
            conn.disconnect();
            Log.e("ChatActivity", "上传媒体文件失败，响应码: " + responseCode);
        } catch (Exception e) {
            Log.e("ChatActivity", "上传媒体文件异常", e);
        }
        return null;
    }

    @Nullable
    private String convertImageToBase64(@NonNull Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return null;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap == null) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (IOException e) {
            return null;
        }
    }

    private void startVoiceRecordingInternal() {
        try {
            recordingFile = File.createTempFile("voice_", ".m4a", getCacheDir());
            Log.d("ChatActivity", "[VOICE] 创建录音文件: " + recordingFile.getAbsolutePath());
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(96000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(recordingFile.getAbsolutePath());
            
            Log.d("ChatActivity", "[VOICE] 准备录音, 文件路径: " + recordingFile.getAbsolutePath());
            mediaRecorder.prepare();
            Log.d("ChatActivity", "[VOICE] MediaRecorder.prepare() 成功");
            
            mediaRecorder.start();
            Log.d("ChatActivity", "[VOICE] MediaRecorder.start() 成功，录音已开始");
            
            if (webInterface != null) {
                webInterface.notifyVoiceRecordState(true);
                Log.d("ChatActivity", "[VOICE] 已通知JS录音状态: true");
            }
            showToast("开始录音...");
        } catch (Exception e) {
            Log.e("ChatActivity", "[VOICE] 录音启动失败", e);
            stopVoiceRecorderInternal(false);
            showToast("录音启动失败: " + e.getMessage());
        }
    }

    private void stopVoiceRecorderInternal(boolean sendMessage) {
        Log.d("ChatActivity", "[VOICE] stopVoiceRecorderInternal called, sendMessage=" + sendMessage);
        
        if (mediaRecorder == null) {
            Log.d("ChatActivity", "[VOICE] mediaRecorder is null, nothing to stop");
            if (webInterface != null) {
                webInterface.notifyVoiceRecordState(false);
            }
            return;
        }
        
        try {
            mediaRecorder.stop();
            Log.d("ChatActivity", "[VOICE] mediaRecorder.stop() 成功");
        } catch (RuntimeException e) {
            Log.e("ChatActivity", "[VOICE] mediaRecorder.stop() 失败: " + e.getMessage());
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d("ChatActivity", "[VOICE] mediaRecorder released");
        }
        
        if (webInterface != null) {
            webInterface.notifyVoiceRecordState(false);
        }
        
        if (recordingFile != null && sendMessage) {
            Log.d("ChatActivity", "[VOICE] 处理录音文件: " + recordingFile.getAbsolutePath() + ", 存在=" + recordingFile.exists() + ", 大小=" + (recordingFile.exists() ? recordingFile.length() : 0));
            
            // 文件已经在正确位置（getCacheDir()），直接使用
            handleAudioResult(recordingFile);
        } else if (recordingFile != null) {
            // 取消录音，删除文件
            Log.d("ChatActivity", "[VOICE] 取消录音，删除文件: " + recordingFile.getAbsolutePath());
            if (recordingFile.exists()) {
                recordingFile.delete();
            }
        }
        recordingFile = null;
        Log.d("ChatActivity", "[VOICE] stopVoiceRecorderInternal 完成");
    }

    private void copyFile(@NonNull File src, @NonNull File dest) {
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.e("ChatActivity", "copyFile failed", e);
        }
    }

    private void playAudio(@NonNull Uri uri) {
        Log.d("ChatActivity", "[VOICE] playAudio 开始, URI: " + uri);
        releaseMediaPlayer();
        
        // 通知WebView开始播放
        if (webInterface != null) {
            webInterface.notifyAudioPlaying(uri.toString(), true);
        }
        
        mediaPlayer = new MediaPlayer();
        try {
            Log.d("ChatActivity", "[VOICE] 设置数据源: " + uri);
            mediaPlayer.setDataSource(this, uri);
            Log.d("ChatActivity", "[VOICE] 准备播放");
            mediaPlayer.prepare();
            Log.d("ChatActivity", "[VOICE] 开始播放");
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("ChatActivity", "[VOICE] 播放完成");
                // 通知WebView停止播放
                if (webInterface != null) {
                    webInterface.notifyAudioPlaying(uri.toString(), false);
                }
                releaseMediaPlayer();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("ChatActivity", "[VOICE] 播放错误: what=" + what + ", extra=" + extra);
                // 通知WebView停止播放
                if (webInterface != null) {
                    webInterface.notifyAudioPlaying(uri.toString(), false);
                }
                releaseMediaPlayer();
                showToast("音频播放失败");
                return true;
            });
            Log.d("ChatActivity", "[VOICE] playAudio 完成");
        } catch (Exception e) {
            Log.e("ChatActivity", "[VOICE] 音频播放失败", e);
            // 通知WebView停止播放
            if (webInterface != null) {
                webInterface.notifyAudioPlaying(uri.toString(), false);
            }
            showToast("音频播放失败: " + e.getMessage());
            releaseMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignore) {
            }
            mediaPlayer = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleBackNavigation() {
        if (mAgentWeb != null && mAgentWeb.getWebCreator().getWebView().canGoBack()) {
            mAgentWeb.getWebCreator().getWebView().goBack();
        } else {
            finish();
        }
    }
}
