package com.example.tongyangyuan.payment;

import android.text.TextUtils;

import java.util.Map;

/**
 * 支付宝支付结果
 */
public class AlipayResult {
    
    private String resultStatus;
    private String result;
    private String memo;
    
    public AlipayResult(Map<String, String> rawResult) {
        if (rawResult == null) {
            return;
        }
        
        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, "resultStatus")) {
                resultStatus = rawResult.get(key);
            } else if (TextUtils.equals(key, "result")) {
                result = rawResult.get(key);
            } else if (TextUtils.equals(key, "memo")) {
                memo = rawResult.get(key);
            }
        }
    }
    
    /**
     * 判断支付是否成功
     * "9000" 表示支付成功
     */
    public boolean isSuccess() {
        return TextUtils.equals(resultStatus, "9000");
    }
    
    /**
     * 判断支付是否正在处理中
     * "8000" 表示支付结果因为支付渠道原因或者系统原因还在等待支付结果确认
     */
    public boolean isProcessing() {
        return TextUtils.equals(resultStatus, "8000");
    }
    
    /**
     * 判断支付是否失败
     */
    public boolean isFailed() {
        return !isSuccess() && !isProcessing();
    }
    
    /**
     * 获取结果状态码
     */
    public String getResultStatus() {
        return resultStatus;
    }
    
    /**
     * 获取结果信息
     */
    public String getResult() {
        return result;
    }
    
    /**
     * 获取备注信息
     */
    public String getMemo() {
        return memo;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (resultStatus) {
            case "9000":
                return "支付成功";
            case "8000":
                return "支付处理中";
            case "4000":
                return "支付失败";
            case "5000":
                return "重复请求";
            case "6001":
                return "用户取消";
            case "6002":
                return "网络连接错误";
            case "6004":
                return "支付结果未知";
            default:
                return "未知状态: " + resultStatus;
        }
    }
    
    @Override
    public String toString() {
        return "AlipayResult{" +
                "resultStatus='" + resultStatus + '\'' +
                ", result='" + result + '\'' +
                ", memo='" + memo + '\'' +
                '}';
    }
}
