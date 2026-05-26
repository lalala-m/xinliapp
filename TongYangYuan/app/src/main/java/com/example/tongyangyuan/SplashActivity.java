package com.example.tongyangyuan;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.database.NetworkConfig;
import com.example.tongyangyuan.webview.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashImage;
    private TextView skipButton;
    private TextView adTitle;
    private CountDownTimer countDownTimer;
    private PreferenceStore preferenceStore;
    private ExecutorService executor;
    
    private String adLinkUrl = "";
    private String adLinkType = "NONE";
    private long adId = -1;
    private int durationSeconds = 3;
    private boolean isSkippable = true;
    private boolean isAdLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        preferenceStore = new PreferenceStore(this);
        executor = Executors.newSingleThreadExecutor();
        
        splashImage = findViewById(R.id.splashImage);
        skipButton = findViewById(R.id.skipButton);
        adTitle = findViewById(R.id.adTitle);
        
        skipButton.setOnClickListener(v -> goToNextScreen());
        splashImage.setOnClickListener(v -> handleAdClick());
        
        // 加载开屏广告
        loadSplashAd();
        
        // 同步咨询师数据（在后台）
        syncConsultants();
    }

    private void loadSplashAd() {
        android.util.Log.d("SplashActivity", "loadSplashAd: starting...");
        executor.execute(() -> {
            try {
                String baseUrl = NetworkConfig.getBaseUrl().replace("/api", "");
                URL url = new URL(baseUrl + "/api/splash-ads/active");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    android.util.Log.d("SplashActivity", "loadSplashAd: response=" + response.toString());
                    JSONObject json = new JSONObject(response.toString());
                    if (json.getInt("code") == 200) {
                        JSONArray data = json.getJSONArray("data");
                        if (data.length() > 0) {
                            JSONObject ad = data.getJSONObject(0);
                            parseAndShowAd(ad);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("SplashActivity", "loadSplashAd error", e);
                e.printStackTrace();
            }
            
            // 无广告或加载失败，直接跳转
            new Handler(Looper.getMainLooper()).post(this::goToNextScreen);
        });
    }

    private void parseAndShowAd(JSONObject ad) {
        android.util.Log.d("SplashActivity", "parseAndShowAd: " + ad.toString());
        try {
            adId = ad.getLong("id");
            String imageUrl = ad.getString("imageUrl");
            String title = ad.optString("title", "");
            adLinkUrl = ad.optString("linkUrl", "");
            adLinkType = ad.optString("linkType", "NONE");
            durationSeconds = ad.optInt("durationSeconds", 3);
            isSkippable = ad.optBoolean("isSkippable", true);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!title.isEmpty()) {
                    adTitle.setText(title);
                    adTitle.setVisibility(View.VISIBLE);
                }
                
                // 加载广告图片
                // 注意：图片路径 /uploads/ 需要通过 /api/uploads/ 访问（因为 server.servlet.context-path=/api）
                String fullUrl = imageUrl.startsWith("http") ? imageUrl : 
                        NetworkConfig.getBaseUrl() + imageUrl;
                android.util.Log.d("SplashActivity", "Loading image: " + fullUrl);
                
                Glide.with(this)
                        .load(fullUrl)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                android.util.Log.d("SplashActivity", "Image loaded successfully");
                                splashImage.setImageDrawable(resource);
                                isAdLoaded = true;
                                startCountDown();
                                recordShow();
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                android.util.Log.e("SplashActivity", "Image load failed");
                                goToNextScreen();
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                        });
            });
        } catch (Exception e) {
            android.util.Log.e("SplashActivity", "loadSplashAd error", e);
                e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(this::goToNextScreen);
        }
    }

    private void startCountDown() {
        if (isSkippable) {
            skipButton.setVisibility(View.VISIBLE);
        }
        
        countDownTimer = new CountDownTimer(durationSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) + 1;
                if (isSkippable) {
                    skipButton.setText("跳过 " + seconds + "s");
                }
            }

            @Override
            public void onFinish() {
                goToNextScreen();
            }
        }.start();
    }

    private void handleAdClick() {
        if (!isAdLoaded || adId < 0) return;
        
        // 记录点击
        recordClick();
        
        // 取消倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        if ("NONE".equals(adLinkType) || adLinkUrl.isEmpty()) {
            goToNextScreen();
            return;
        }
        
        // 处理跳转
        Intent intent;
        if ("EXTERNAL".equals(adLinkType)) {
            intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(adLinkUrl));
        } else {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, adLinkUrl);
        }
        
        startActivity(intent);
        finish();
    }

    private void recordShow() {
        if (adId < 0) return;
        executor.execute(() -> {
            try {
                String baseUrl = NetworkConfig.getBaseUrl().replace("/api", "");
                URL url = new URL(baseUrl + "/api/splash-ads/" + adId + "/show");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(3000);
                conn.getResponseCode();
            } catch (Exception e) {
                android.util.Log.e("SplashActivity", "loadSplashAd error", e);
                e.printStackTrace();
            }
        });
    }

    private void recordClick() {
        if (adId < 0) return;
        executor.execute(() -> {
            try {
                String baseUrl = NetworkConfig.getBaseUrl().replace("/api", "");
                URL url = new URL(baseUrl + "/api/splash-ads/" + adId + "/click");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(3000);
                conn.getResponseCode();
            } catch (Exception e) {
                android.util.Log.e("SplashActivity", "loadSplashAd error", e);
                e.printStackTrace();
            }
        });
    }

    private void goToNextScreen() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        Intent intent;
        if (!preferenceStore.isLoggedIn()) {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "auth.html");
        } else if (!preferenceStore.hasChildProfile()) {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "child_info.html");
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void syncConsultants() {
        // 在后台线程同步咨询师数据
        new Thread(() -> {
            try {
                com.example.tongyangyuan.database.AppDatabase db = 
                        com.example.tongyangyuan.database.AppDatabase.getInstance(this);
                int localCount = db.consultantDao().getCount();
                
                if (localCount == 0) {
                    // 本地无数据时，执行全量同步
                    DataSyncService.getInstance(this).syncConsultants(new DataSyncService.SyncCallback() {
                        @Override
                        public void onSuccess(int count) {
                            android.util.Log.d("SplashActivity", "Consultants synced: " + count);
                        }

                        @Override
                        public void onError(Exception e) {
                            android.util.Log.e("SplashActivity", "Sync failed", e);
                        }
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("SplashActivity", "syncConsultants error", e);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
