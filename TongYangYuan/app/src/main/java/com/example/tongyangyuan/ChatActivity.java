package com.example.tongyangyuan;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.ViewGroup;
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
        loadChatPage();

        initStomp();
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
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(null);

        // 允许 file:// 页面发起跨域请求（SockJS 需要）
        android.webkit.WebSettings webSettings = mAgentWeb.getWebCreator().getWebView().getSettings();
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        webInterface = new WebAppInterface(this, mAgentWeb.getWebCreator().getWebView());
        webInterface.setMediaDelegate(this);
        mAgentWeb.getJsInterfaceHolder().addJavaObject("Android", webInterface);
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

    private void loadChatPage() {
        StringBuilder url = new StringBuilder("file:///android_asset/chat.html");
        String delimiter = "?";
        if (!TextUtils.isEmpty(appointmentId)) {
            url.append(delimiter).append("appointmentId=").append(Uri.encode(appointmentId));
            delimiter = "&";
        }
        if (consultant != null) {
            url.append(delimiter).append("consultant=").append(Uri.encode(consultant.getName()));
            delimiter = "&";
            if (!TextUtils.isEmpty(consultant.getTitle())) {
                url.append(delimiter).append("title=").append(Uri.encode(consultant.getTitle()));
                delimiter = "&";
            }
            if (consultant.getUserId() > 0) {
                url.append(delimiter).append("consultantUserId=").append(consultant.getUserId());
                delimiter = "&";
            }
        }
        if (!TextUtils.isEmpty(appointmentDate)) {
            url.append(delimiter).append("date=").append(Uri.encode(appointmentDate));
            delimiter = "&";
        }
        if (!TextUtils.isEmpty(appointmentSlot)) {
            url.append(delimiter).append("slot=").append(Uri.encode(appointmentSlot));
            delimiter = "&";
        }
        if (!TextUtils.isEmpty(childId)) {
            url.append(delimiter).append("childId=").append(Uri.encode(childId));
            delimiter = "&";
        }
        if (!TextUtils.isEmpty(childName)) {
            url.append(delimiter).append("childName=").append(Uri.encode(childName));
            delimiter = "&";
        }
        if (serverAppointmentId != -1) {
            url.append(delimiter).append("serverAppointmentId=").append(serverAppointmentId);
        }
        mAgentWeb.getUrlLoader().loadUrl(url.toString());
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
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                audioFile
        );
        if (webInterface != null) {
            webInterface.notifyMediaMessage("audio", "语音消息", uri.toString());
        }
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
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(96000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(recordingFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            if (webInterface != null) {
                webInterface.notifyVoiceRecordState(true);
            }
            showToast("开始录音...");
        } catch (Exception e) {
            stopVoiceRecorderInternal(false);
            showToast("录音启动失败");
        }
    }

    private void stopVoiceRecorderInternal(boolean sendMessage) {
        if (mediaRecorder == null) {
            if (webInterface != null) {
                webInterface.notifyVoiceRecordState(false);
            }
            return;
        }
        try {
            mediaRecorder.stop();
        } catch (RuntimeException ignore) {
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (webInterface != null) {
            webInterface.notifyVoiceRecordState(false);
        }
        if (recordingFile != null && sendMessage) {
            File targetDir = new File(getFilesDir(), "voices");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            File targetFile = new File(targetDir, recordingFile.getName());
            if (!recordingFile.equals(targetFile)) {
                copyFile(recordingFile, targetFile);
            }
            handleAudioResult(targetFile);
        }
        if (recordingFile != null && recordingFile.exists()) {
            recordingFile.delete();
        }
        recordingFile = null;
    }

    private void copyFile(@NonNull File source, @NonNull File dest) {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            showToast("保存语音失败");
        }
    }

    private void playAudio(Uri uri) {
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
            mediaPlayer.prepare();
            mediaPlayer.start();
            showToast("正在播放语音");
        } catch (IOException e) {
            showToast("语音播放失败");
            releaseMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleBackNavigation() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("TARGET_FRAGMENT", "MESSAGE_FRAGMENT");
        startActivity(intent);
        finish();
    }

}
