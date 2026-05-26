package com.example.tongyangyuan.payment;

import android.util.Base64;
import android.util.Log;

import com.example.tongyangyuan.payment.config.AlipayConfig;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 支付宝支付工具类
 * 处理签名、验签、订单信息构建
 */
public class AlipayUtil {
    
    private static final String TAG = "AlipayUtil";
    
    /**
     * 构建支付宝订单信息
     * 
     * @param orderId 商户订单号
     * @param amount 金额（元）
     * @param subject 商品标题
     * @param body 商品描述
     * @return 签名的订单信息字符串
     */
    public static String buildOrderInfo(String orderId, String amount, String subject, String body) {
        try {
            // 检查配置
            if (!AlipayConfig.isConfigValid()) {
                Log.e(TAG, "支付宝配置不完整，缺少: " + AlipayConfig.getMissingConfig());
                return null;
            }
            
            // 构建请求参数
            Map<String, String> params = new TreeMap<>();
            params.put("app_id", AlipayConfig.APP_ID);
            params.put("method", "alipay.trade.app.pay");
            params.put("charset", AlipayConfig.CHARSET);
            params.put("sign_type", AlipayConfig.SIGN_TYPE);
            params.put("timestamp", getCurrentTime());
            params.put("version", "1.0");
            params.put("notify_url", AlipayConfig.NOTIFY_URL);
            
            // 业务参数
            Map<String, String> bizContent = new TreeMap<>();
            bizContent.put("out_trade_no", orderId);
            bizContent.put("total_amount", amount);
            bizContent.put("subject", subject);
            bizContent.put("body", body);
            bizContent.put("product_code", "QUICK_MSECURITY_PAY");
            
            params.put("biz_content", mapToJson(bizContent));
            
            // 生成签名
            String sign = sign(getSignContent(params), AlipayConfig.APP_PRIVATE_KEY);
            params.put("sign", sign);
            
            // 构建最终的订单信息字符串
            return buildOrderParamString(params);
            
        } catch (Exception e) {
            Log.e(TAG, "构建订单信息失败", e);
            return null;
        }
    }
    
    /**
     * 使用私钥对内容签名
     */
    public static String sign(String content, String privateKey) throws Exception {
        // 去除私钥中的换行和标记
        privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.decode(privateKey, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(keySpec);
        
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        
        byte[] signed = signature.sign();
        return Base64.encodeToString(signed, Base64.NO_WRAP);
    }
    
    /**
     * 使用支付宝公钥验签
     */
    public static boolean verify(String content, String sign, String publicKey) throws Exception {
        // 去除公钥中的换行和标记
        publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.decode(publicKey, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(pubKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        
        byte[] signBytes = Base64.decode(sign, Base64.DEFAULT);
        return signature.verify(signBytes);
    }
    
    /**
     * 获取待签名内容
     */
    private static String getSignContent(Map<String, String> params) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.equals("sign") && value != null && !value.isEmpty()) {
                if (content.length() > 0) {
                    content.append("&");
                }
                content.append(key).append("=").append(value);
            }
        }
        return content.toString();
    }
    
    /**
     * 构建订单参数字符串（URL编码格式）
     */
    private static String buildOrderParamString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(encode(entry.getValue()));
        }
        return sb.toString();
    }
    
    /**
     * URL编码
     */
    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
    
    /**
     * Map转JSON字符串（简化版）
     */
    private static String mapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (i > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            i++;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * 获取当前时间字符串
     */
    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdf.format(new Date());
    }
    
    /**
     * 生成随机订单号
     */
    public static String generateOrderId() {
        String prefix = "TYY";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(new Random().nextInt(9000) + 1000);
        return prefix + timestamp + random;
    }
}
