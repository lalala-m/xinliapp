package com.example.tongyangyuan;

import android.app.Application;
import android.util.Log;

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
