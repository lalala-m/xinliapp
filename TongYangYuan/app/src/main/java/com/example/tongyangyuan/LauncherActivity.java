package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.webview.WebViewActivity;

public class LauncherActivity extends AppCompatActivity {

    private PreferenceStore preferenceStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        preferenceStore = new PreferenceStore(this);
        
        // 启动时同步咨询师数据
        syncData();
        
        new Handler(Looper.getMainLooper()).post(this::showSplash);
    }

    private void syncData() {
        DataSyncService.getInstance(this).syncConsultants(new DataSyncService.SyncCallback() {
            @Override
            public void onSuccess(int count) {
                new Handler(Looper.getMainLooper()).post(() -> 
                    android.widget.Toast.makeText(getApplicationContext(), "数据同步成功: " + count + "位咨询师", android.widget.Toast.LENGTH_SHORT).show()
                );
                
                // 同步预约记录
                new com.example.tongyangyuan.data.AppointmentStore(getApplicationContext()).syncFromServer();
            }

            @Override
            public void onError(Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> 
                    android.widget.Toast.makeText(getApplicationContext(), "数据同步失败: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    private void showSplash() {
        Intent splashIntent = new Intent(this, WebViewActivity.class);
        splashIntent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "splash.html");
        splashIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(splashIntent);
        new Handler(Looper.getMainLooper()).postDelayed(this::routeNext, 1600);
    }

    private void routeNext() {
        if (preferenceStore.isFirstLaunch()) {
            preferenceStore.markFirstLaunchComplete();
        }

        if (!preferenceStore.isLoggedIn()) {
            openWebPage("auth.html");
            return;
        }

        if (!preferenceStore.hasChildProfile()) {
            openWebPage("child_info.html");
            return;
        }

        // Logged in and has profile, go to main activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void openWebPage(String htmlFile) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, htmlFile);
        startActivity(intent);
        finish();
    }
}

