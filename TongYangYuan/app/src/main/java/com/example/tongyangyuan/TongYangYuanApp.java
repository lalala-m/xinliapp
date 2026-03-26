package com.example.tongyangyuan;

import android.app.Application;
import android.util.Log;

import com.example.tongyangyuan.data.AppointmentStore;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.openim.OpenIMService;

public class TongYangYuanApp extends Application {
    private static final String TAG = "TongYangYuanApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 OpenIM SDK（信令层）
        OpenIMService.getInstance(this).init();
        Log.d(TAG, "OpenIM SDK initialized");

        // 同步 MySQL 数据
        syncDataFromMySQL();

        // 开启预约定时同步（已登录用户每30秒拉取一次后端）
        PreferenceStore prefStore = new PreferenceStore(this);
        if (prefStore.getUserId() > 0) {
            AppointmentStore appointmentStore = new AppointmentStore(this);
            appointmentStore.startPeriodicSync(this);
            Log.d(TAG, "Appointment periodic sync started");
        }
    }

    private void syncDataFromMySQL() {
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
    }
}
