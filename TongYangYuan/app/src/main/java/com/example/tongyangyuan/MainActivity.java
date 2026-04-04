package com.example.tongyangyuan;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tongyangyuan.fragment.HomeFragment;
import com.example.tongyangyuan.fragment.MessageFragment;
import com.example.tongyangyuan.fragment.ProfileFragment;
import com.example.tongyangyuan.fragment.SocialFragment;
import com.example.tongyangyuan.webview.WebAppInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements WebAppInterface.MediaDelegate {

    private BottomNavigationView bottomNavigation;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private ActivityResultLauncher<Uri> captureImageLauncher;
    private ActivityResultLauncher<Uri> captureVideoLauncher;
    private ActivityResultLauncher<String> audioPermissionLauncher;
    
    private Uri pendingImageUri;
    private Uri pendingVideoUri;
    private WebAppInterface webInterface; // 用于回调

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        initViews();
        registerLaunchers();
        setupBottomNavigation();

        // 适配 Edge-to-Edge，防止底部导航栏被系统导航栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        // 适配顶部状态栏，防止内容被遮挡
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainer), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        
        handleIntent(getIntent());

        // 默认显示首页
        if (savedInstanceState == null && getIntent().getStringExtra("TARGET_FRAGMENT") == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && "MESSAGE_FRAGMENT".equals(intent.getStringExtra("TARGET_FRAGMENT"))) {
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_message);
            }
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            // } else if (itemId == R.id.nav_social) {
            //    fragment = new SocialFragment();
            } else if (itemId == R.id.nav_message) {
                fragment = new MessageFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }
            
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
    
    public void setBottomNavDimmed(boolean dimmed) {
        if (bottomNavigation == null) {
            return;
        }
        float alpha = dimmed ? 0.7f : 1.0f;
        bottomNavigation.setAlpha(alpha);
    }
    
    // 设置当前的 WebAppInterface 实例，以便 Fragment 调用 setMediaDelegate 时，MainActivity 知道往哪里回传数据
    // 注意：WebAppInterface.java 中 notifyMediaMessage 是直接调用 dispatchJs。
    // 由于 WebAppInterface 是在 Fragment 中实例化的，我们如何获取到它？
    // 实际上 WebAppInterface 是持有 Context 的，如果 Context 是 MainActivity，那么
    // WebAppInterface 会调用 MainActivity.pickImage()。
    // 当 MainActivity 拿到结果后，需要调用 WebAppInterface.notifyMediaMessage()。
    // 但是 WebAppInterface 实例是在 Fragment 里的 WebView 创建的。
    // 简单的办法：MainActivity 维护一个当前的 WebAppInterface 引用，
    // 或者发送广播/EventBus。
    // 更好的办法：WebAppInterface 内部持有 WebView，它自己就是 callback 的入口。
    // 当我们调用 MainActivity.pickImage() 时，我们不知道是哪个 WebAppInterface 发起的。
    // 但是我们可以假设当前活动的 Fragment 的 WebAppInterface 是目标。
    // 或者，我们在 WebAppInterface 中注册自己到 MainActivity。
    
    public void setCurrentWebInterface(WebAppInterface webInterface) {
        this.webInterface = webInterface;
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
        
        // 录音权限略
        audioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        Toast.makeText(this, "录音权限被拒绝", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    
    private void takePersistablePermission(@NonNull Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException ignore) {
            }
        }
    }
    
    private void handleImageResult(@NonNull Uri uri) {
        String base64 = convertImageToBase64(uri);
        if (base64 == null) {
            Toast.makeText(this, "图片读取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        String dataUrl = "data:image/jpeg;base64," + base64;
        if (webInterface != null) {
            webInterface.notifyMediaMessage("image", "已选择图片", dataUrl);
        }
    }

    private void handleVideoResult(@NonNull Uri uri) {
        if (webInterface != null) {
            webInterface.notifyMediaMessage("video", "已选择视频", uri.toString());
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

    @Override
    public void pickImage() {
        pickImageLauncher.launch("image/*");
    }

    @Override
    public void captureImage() {
        pendingImageUri = createImageUri();
        if (pendingImageUri != null) {
            captureImageLauncher.launch(pendingImageUri);
        } else {
            Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void pickVideo() {
        pickVideoLauncher.launch("video/*");
    }

    @Override
    public void captureVideo() {
        pendingVideoUri = createVideoUri();
        if (pendingVideoUri != null) {
            captureVideoLauncher.launch(pendingVideoUri);
        } else {
            Toast.makeText(this, "无法创建视频文件", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startVoiceRecord() {
        // MainActivity 暂不实现录音，或留空
    }

    @Override
    public void stopVoiceRecord() {
        // MainActivity 暂不实现录音，或留空
    }

    @Override
    public void cancelVoiceRecord() {
        // MainActivity 暂不实现录音，或留空
    }

    @Override
    public void playMedia(String type, String uri) {
        // 简单的播放实现，或者留空
        if (uri == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(uri), type.equals("video") ? "video/*" : "audio/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法播放媒体", Toast.LENGTH_SHORT).show();
        }
    }
}
