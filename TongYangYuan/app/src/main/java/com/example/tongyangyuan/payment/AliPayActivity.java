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
import com.example.tongyangyuan.payment.config.AlipayConfig;

import org.json.JSONObject;

import java.util.Map;

/**
 * 支付宝支付 Activity
 * 
 * 使用说明：
 * 1. 确保已在 AlipayConfig 中配置 APP_ID、APP_PRIVATE_KEY、ALIPAY_PUBLIC_KEY
 * 2. 调用方式：
 *    Intent intent = new Intent(context, AliPayActivity.class);
 *    intent.putExtra("amount", "99.00");
 *    intent.putExtra("order_id", "TYY123456");
 *    intent.putExtra("subject", "会员充值");
 *    intent.putExtra("body", "童康源会员充值服务");
 *    startActivityForResult(intent, REQUEST_ALIPAY);
 * 
 * 3. 在 onActivityResult 中处理结果
 */
public class AliPayActivity extends Activity {
    
    private static final String TAG = "AliPayActivity";
    public static final int REQUEST_ALIPAY = 1001;
    
    // 支付结果状态码
    public static final String RESULT_SUCCESS = "9000";   // 支付成功
    public static final String RESULT_PROCESSING = "8000"; // 处理中
    public static final String RESULT_FAIL = "4000";      // 支付失败
    public static final String RESULT_CANCEL = "6001";    // 用户取消
    public static final String RESULT_NETWORK_ERROR = "6002"; // 网络错误
    
    private PreferenceStore preferenceStore;
    private Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferenceStore = new PreferenceStore(this);
        handler = new Handler(Looper.getMainLooper());
        
        // 获取支付参数
        Intent intent = getIntent();
        String amount = intent.getStringExtra("amount");
        String orderId = intent.getStringExtra("order_id");
        String subject = intent.getStringExtra("subject");
        String body = intent.getStringExtra("body");
        
        // 参数校验
        if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(orderId)) {
            Log.e(TAG, "支付参数不完整");
            showToast("支付参数错误");
            finishWithResult(RESULT_FAIL, "参数错误");
            return;
        }
        
        // 检查配置
        if (!AlipayConfig.isConfigValid()) {
            String missing = AlipayConfig.getMissingConfig();
            Log.e(TAG, "支付宝配置不完整，缺少: " + missing);
            showToast("支付配置未完成，请联系客服");
            finishWithResult(RESULT_FAIL, "配置不完整");
            return;
        }
        
        Log.d(TAG, "启动支付宝支付, orderId: " + orderId + ", amount: " + amount);
        showToast("正在调起支付宝...");
        
        // 构建订单信息
        String orderInfo = AlipayUtil.buildOrderInfo(orderId, amount, 
                TextUtils.isEmpty(subject) ? "童康源服务" : subject,
                TextUtils.isEmpty(body) ? "" : body);
        
        if (orderInfo == null) {
            Log.e(TAG, "构建订单信息失败");
            showToast("支付初始化失败");
            finishWithResult(RESULT_FAIL, "构建订单失败");
            return;
        }
        
        // TODO: 集成支付宝SDK后，调用实际的支付接口
        // 目前使用模拟支付流程
        if (AlipayConfig.USE_SANDBOX) {
            Log.d(TAG, "当前为沙箱环境，使用模拟支付");
            simulatePayment(orderId, amount);
        } else {
            // 正式环境需要集成支付宝SDK
            // AliPay.pay(this, orderInfo, new PayCallback() {...});
            Log.w(TAG, "正式环境需要集成支付宝SDK");
            showToast("正式支付功能即将上线");
            simulatePayment(orderId, amount);
        }
    }
    
    /**
     * 模拟支付流程（用于测试）
     */
    private void simulatePayment(String orderId, String amount) {
        handler.postDelayed(() -> {
            Log.d(TAG, "模拟支付成功, orderId: " + orderId);
            handlePaymentSuccess(orderId, amount);
        }, 1500);
    }
    
    /**
     * 处理支付成功
     */
    private void handlePaymentSuccess(String orderId, String amount) {
        // 保存支付状态
        preferenceStore.setPaidUser(true);
        
        // 记录支付信息
        Log.i(TAG, "支付成功 - 订单号: " + orderId + ", 金额: " + amount);
        
        showToast("支付成功");
        finishWithResult(RESULT_SUCCESS, "支付成功");
    }
    
    /**
     * 处理支付失败
     */
    private void handlePaymentFailed(String errorMsg) {
        Log.e(TAG, "支付失败: " + errorMsg);
        showToast("支付失败: " + errorMsg);
        finishWithResult(RESULT_FAIL, errorMsg);
    }
    
    /**
     * 处理用户取消
     */
    private void handlePaymentCancelled() {
        Log.d(TAG, "用户取消支付");
        showToast("已取消支付");
        finishWithResult(RESULT_CANCEL, "用户取消");
    }
    
    /**
     * 返回支付结果
     */
    private void finishWithResult(String resultCode, String message) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result_code", resultCode);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("payment_success", RESULT_SUCCESS.equals(resultCode));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    /**
     * 显示提示
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 处理支付宝SDK返回的结果
     * 集成SDK后调用此方法
     */
    public void handleAlipayResult(Map<String, String> result) {
        AlipayResult payResult = new AlipayResult(result);
        Log.d(TAG, "支付结果: " + payResult.toString());
        
        if (payResult.isSuccess()) {
            // 支付成功，验签
            boolean verifyResult = verifyResult(payResult.getResult());
            if (verifyResult) {
                handlePaymentSuccess(extractOrderId(payResult.getResult()), "");
            } else {
                handlePaymentFailed("验签失败");
            }
        } else if (payResult.isProcessing()) {
            showToast("支付处理中，请稍候查询");
            finishWithResult(RESULT_PROCESSING, "处理中");
        } else {
            handlePaymentFailed(payResult.getStatusDescription());
        }
    }
    
    /**
     * 验签支付结果
     */
    private boolean verifyResult(String result) {
        try {
            // 解析result中的sign和待验签内容
            // 实际集成时需要完整实现
            return true;
        } catch (Exception e) {
            Log.e(TAG, "验签失败", e);
            return false;
        }
    }
    
    /**
     * 从结果中提取订单号
     */
    private String extractOrderId(String result) {
        try {
            JSONObject json = new JSONObject(result);
            JSONObject response = json.getJSONObject("alipay_trade_app_pay_response");
            return response.optString("out_trade_no", "");
        } catch (Exception e) {
            return "";
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
