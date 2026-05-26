package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tongyangyuan.child.ChildProfileRepository;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.webview.WebViewActivity;

public class LauncherActivity extends AppCompatActivity {

    private PreferenceStore preferenceStore;
    private volatile boolean childSyncDone = false;
    private volatile boolean consultantSyncDone = false;
    private static final long SPLASH_DELAY_MS = 800;
    private static final long MAX_SYNC_WAIT_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        preferenceStore = new PreferenceStore(this);
        
        // 启动时同步数据
        syncData();
        
        new Handler(Looper.getMainLooper()).post(this::showSplash);
    }

    private void syncData() {
        new Thread(() -> {
            try {
                // 1. 已登录用户：同步孩子信息从服务器（最高优先级）
                if (preferenceStore.isLoggedIn()) {
                    String userPhone = preferenceStore.getLastLoginPhone();
                    if (userPhone != null && !userPhone.isEmpty()) {
                        final Object lock = new Object();
                        ChildProfileRepository.getInstance(this).syncFromServer(userPhone, 
                            new ChildProfileRepository.SaveCallback() {
                                @Override
                                public void onSuccess() {
                                    android.util.Log.d("LauncherActivity", "孩子信息同步成功");
                                    childSyncDone = true;
                                    synchronized (lock) { lock.notify(); }
                                }
                                @Override
                                public void onError(Exception e) {
                                    android.util.Log.e("LauncherActivity", "孩子信息同步失败", e);
                                    childSyncDone = true;
                                    synchronized (lock) { lock.notify(); }
                                }
                            });
                        // 最多等待3秒
                        synchronized (lock) {
                            if (!childSyncDone) {
                                lock.wait(MAX_SYNC_WAIT_MS);
                            }
                        }
                    } else {
                        childSyncDone = true;
                    }
                } else {
                    childSyncDone = true;
                }
                
                // 2. 同步咨询师数据
                com.example.tongyangyuan.database.AppDatabase db = 
                        com.example.tongyangyuan.database.AppDatabase.getInstance(this);
                int localCount = db.consultantDao().getCount();
                
                if (localCount > 0) {
                    new com.example.tongyangyuan.data.AppointmentStore(getApplicationContext()).syncFromServer();
                    consultantSyncDone = true;
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        DataSyncService.getInstance(this).syncConsultants(new DataSyncService.SyncCallback() {
                            @Override
                            public void onSuccess(int count) {
                                android.widget.Toast.makeText(getApplicationContext(), "数据同步成功: " + count + "位咨询师", android.widget.Toast.LENGTH_SHORT).show();
                                new com.example.tongyangyuan.data.AppointmentStore(getApplicationContext()).syncFromServer();
                                consultantSyncDone = true;
                            }
                            @Override
                            public void onError(Exception e) {
                                consultantSyncDone = true;
                            }
                        });
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("LauncherActivity", "syncData error", e);
                childSyncDone = true;
                consultantSyncDone = true;
            }
        }).start();
    }

    private void showSplash() {
        new Handler(Looper.getMainLooper()).postDelayed(this::routeNext, SPLASH_DELAY_MS);
    }

    private void routeNext() {
        if (preferenceStore.isFirstLaunch()) {
            preferenceStore.markFirstLaunchComplete();
        }

        Intent intent;
        if (!preferenceStore.isLoggedIn()) {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "auth.html");
        } else if (!hasChildProfileSynced()) {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "child_info.html");
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * 检查是否有孩子信息：优先从本地数据库查询（同步后的最新数据）
     */
    private boolean hasChildProfileSynced() {
        try {
            // 先从数据库查询（同步后的真实数据）
            com.example.tongyangyuan.database.AppDatabase db = 
                    com.example.tongyangyuan.database.AppDatabase.getInstance(this);
            String userPhone = preferenceStore.getLastLoginPhone();
            if (userPhone != null && !userPhone.isEmpty()) {
                int count = db.childProfileDao().getCountByUser(userPhone);
                if (count > 0) {
                    // 数据库有数据，更新SharedPreferences
                    preferenceStore.setHasChildProfile(true);
                    return true;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("LauncherActivity", "查询孩子信息失败", e);
        }
        // 数据库无数据，回退到SharedPreferences
        return preferenceStore.hasChildProfile();
    }
}
