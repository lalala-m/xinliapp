package com.example.tongyangyuan.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.tongyangyuan.data.PreferenceStore;

/**
 * 微信支付 Activity (简化版)
 * 用于演示支付流程
 * 实际使用时需要集成微信支付SDK
 */
public class WeChatPayActivity extends Activity {
    
    private static final String TAG = "WeChatPayActivity";
    
    private PreferenceStore preferenceStore;
    private Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferenceStore = new PreferenceStore(this);
        handler = new Handler(Looper.getMainLooper());
        
        // 获取支付参数
        Intent intent = getIntent();
        String payInfo = intent.getStringExtra("pay_info");
        String orderId = intent.getStringExtra("order_id");
        
        if (TextUtils.isEmpty(payInfo)) {
            Log.e(TAG, "支付参数为空");
            showToast("支付参数错误");
            finish();
            return;
        }
        
        Log.d(TAG, "收到微信支付请求, orderId: " + orderId);
        showToast("正在调起微信支付...");
        
        // 模拟支付流程
        // 实际使用时需要调用微信支付SDK
        simulatePayment();
    }
    
    private void simulatePayment() {
        // 模拟支付延迟
        handler.postDelayed(() -> {
            // 模拟支付成功
            Log.d(TAG, "模拟微信支付成功");
            handlePaymentSuccess("模拟支付成功");
        }, 1500);
    }
    
    private void handlePaymentSuccess(String message) {
        preferenceStore.setPaidUser(true);
        showToast("支付成功");
        
        // 返回结果给调用者
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_success", true);
        resultIntent.putExtra("message", message);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    private void handlePaymentFailed(String message) {
        showToast("支付失败");
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_success", false);
        resultIntent.putExtra("message", message);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    private void handlePaymentCancelled() {
        showToast("用户取消支付");
        finish();
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
