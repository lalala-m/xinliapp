package com.tongyangyuan.mentalhealth.dto;

import java.math.BigDecimal;

/**
 * 创建订单请求 DTO
 */
public class CreateOrderRequest {
    
    private String packageCode;  // 套餐代码: month/quarter
    private String paymentMethod;  // 支付方式: alipay/wechat
    private String deviceInfo;  // 设备信息
    private String clientIp;  // 客户端IP
    
    // Getters and Setters
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
}
