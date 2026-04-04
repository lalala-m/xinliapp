package com.example.tongyangyuan;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tongyangyuan.data.AppointmentStore;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.database.NetworkConfig;
import com.example.tongyangyuan.openim.OpenIMService;

public class TongYangYuanApp extends Application {
    private static final String TAG = "TongYangYuanApp";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        // 全局异常捕获，防止闪退
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception in thread " + thread.getName(), throwable);
        });

        try {
            NetworkConfig.initFromPrefs(this);
        } catch (Exception e) {
            Log.e(TAG, "NetworkConfig.initFromPrefs failed", e);
        }

        // 初始化 OpenIM SDK（信令层）
        try {
            OpenIMService.getInstance(this).init();
            Log.d(TAG, "OpenIM SDK initialized");
        } catch (Exception e) {
            Log.e(TAG, "OpenIM SDK init failed", e);
        }

        // 同步 MySQL 数据
        syncDataFromMySQL();

        // 开启预约定时同步（已登录用户每30秒拉取一次后端）
        try {
            PreferenceStore prefStore = new PreferenceStore(this);
            if (prefStore.getUserId() > 0) {
                AppointmentStore appointmentStore = new AppointmentStore(this);
                appointmentStore.startPeriodicSync(this);
                Log.d(TAG, "Appointment periodic sync started");
            }
        } catch (Exception e) {
            Log.e(TAG, "Appointment periodic sync setup failed", e);
        }
    }

    private void syncDataFromMySQL() {
        try {
            DataSyncService syncService = DataSyncService.getInstance(this);
            syncService.syncConsultants(new DataSyncService.SyncCallback() {
                @Override
                public void onSuccess(int count) {
                    Log.d(TAG, "Successfully synced " + count + " consultants from MySQL");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to sync data from MySQL: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "DataSyncService.getInstance failed", e);
        }
    }
}
