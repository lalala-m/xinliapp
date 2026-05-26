package com.tongyangyuan.mentalhealth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单状态响应 DTO
 */
public class OrderStatusResponse {
    
    private boolean success;
    private String message;
    private String orderNo;  // 订单号
    private String packageCode;  // 套餐代码
    private String packageName;  // 套餐名称
    private BigDecimal totalAmount;  // 订单金额
    private String paymentStatus;  // 支付状态
    private String paymentMethod;  // 支付方式
    private LocalDateTime paidTime;  // 支付时间
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime expiresAt;  // 过期时间
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public LocalDateTime getPaidTime() { return paidTime; }
    public void setPaidTime(LocalDateTime paidTime) { this.paidTime = paidTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
