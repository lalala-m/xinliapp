package com.example.tongyangyuan.payment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.database.NetworkConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 支付帮助类
 * 负责与后端支付 API 交互
 */
public class PaymentHelper {
    
    private static final String TAG = "PaymentHelper";
    private static final int TIMEOUT_MS = 15000;
    
    private final Context context;
    private final PreferenceStore preferenceStore;
    private final Handler mainHandler;
    
    // TODO: 实际项目中需要从服务器获取或配置
    // 微信支付 AppID
    private static final String WECHAT_APP_ID = "";
    
    public PaymentHelper(Context context) {
        this.context = context;
        this.preferenceStore = new PreferenceStore(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 创建订单并获取支付参数
     */
    public void createOrder(String packageCode, String paymentMethod, PaymentCallback callback) {
        new Thread(() -> {
            try {
                // 构建请求
                URL url = new URL(NetworkConfig.getBaseUrl() + "/api/payment/create-order");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer user_" + preferenceStore.getUserId());
                conn.setDoOutput(true);
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                
                // 构建请求体
                JSONObject requestBody = new JSONObject();
                requestBody.put("packageCode", packageCode);
                requestBody.put("paymentMethod", paymentMethod);
                
                // 发送请求
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                }
                
                // 读取响应
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                Log.d(TAG, "createOrder response: " + response);
                
                // 解析响应
                JSONObject jsonResponse = new JSONObject(response.toString());
                
                mainHandler.post(() -> {
                    if (responseCode == 200 && jsonResponse.optBoolean("success", false)) {
                        JSONObject data = jsonResponse.optJSONObject("data");
                        if (data != null) {
                            String orderNo = data.optString("orderNo");
                            String payParams = data.optString("payParams");
                            callback.onSuccess(orderNo, payParams, paymentMethod);
                        } else {
                            callback.onError("创建订单失败: 响应数据为空");
                        }
                    } else {
                        String message = jsonResponse.optString("message", "创建订单失败");
                        callback.onError(message);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "createOrder error", e);
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * 查询订单状态
     */
    public void queryOrderStatus(String orderNo, OrderStatusCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(NetworkConfig.getBaseUrl() + "/api/payment/order-status?orderNo=" + orderNo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject data = jsonResponse.optJSONObject("data");
                
                if (data != null) {
                    String status = data.optString("paymentStatus");
                    boolean success = "SUCCESS".equals(status);
                    mainHandler.post(() -> callback.onResult(success, status));
                } else {
                    mainHandler.post(() -> callback.onResult(false, "UNKNOWN"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "queryOrderStatus error", e);
                mainHandler.post(() -> callback.onResult(false, "ERROR"));
            }
        }).start();
    }
    
    /**
     * 获取钱包余额
     */
    public void getWalletBalance(WalletBalanceCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(NetworkConfig.getBaseUrl() + "/api/wallet/balance");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer user_" + preferenceStore.getUserId());
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject data = jsonResponse.optJSONObject("data");
                
                if (data != null) {
                    double balance = data.optDouble("balance", 0);
                    mainHandler.post(() -> callback.onResult(balance));
                } else {
                    mainHandler.post(() -> callback.onResult(0));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "getWalletBalance error", e);
                mainHandler.post(() -> callback.onResult(0));
            }
        }).start();
    }
    
    /**
     * 获取会员状态
     */
    public void getMembershipStatus(MembershipStatusCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(NetworkConfig.getBaseUrl() + "/api/payment/membership-status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer user_" + preferenceStore.getUserId());
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject data = jsonResponse.optJSONObject("data");
                
                if (data != null) {
                    boolean isPaid = data.optBoolean("isPaid", false);
                    long remainingDays = data.optLong("remainingDays", 0);
                    String packageName = data.optString("packageName", "");
                    mainHandler.post(() -> callback.onResult(isPaid, remainingDays, packageName));
                } else {
                    mainHandler.post(() -> callback.onResult(false, 0, ""));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "getMembershipStatus error", e);
                mainHandler.post(() -> callback.onResult(false, 0, ""));
            }
        }).start();
    }
    
    // 回调接口
    public interface PaymentCallback {
        void onSuccess(String orderNo, String payParams, String paymentMethod);
        void onError(String message);
    }
    
    public interface OrderStatusCallback {
        void onResult(boolean success, String status);
    }
    
    public interface WalletBalanceCallback {
        void onResult(double balance);
    }
    
    public interface MembershipStatusCallback {
        void onResult(boolean isPaid, long remainingDays, String packageName);
    }
}
