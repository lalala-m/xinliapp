package com.tongyangyuan.mentalhealth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建订单响应 DTO
 */
public class CreateOrderResponse {
    
    private boolean success;
    private String message;
    private String orderNo;  // 订单号
    private String paymentMethod;  // 支付方式
    private BigDecimal totalAmount;  // 订单金额
    private String payUrl;  // 支付链接/二维码
    private String payParams;  // 调起支付的参数字符串
    private LocalDateTime expiresAt;  // 过期时间
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    
    public String getPayParams() { return payParams; }
    public void setPayParams(String payParams) { this.payParams = payParams; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
