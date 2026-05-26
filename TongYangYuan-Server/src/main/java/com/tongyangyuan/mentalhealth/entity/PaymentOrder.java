package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体
 */
@Entity
@Table(name = "payment_orders")
public class PaymentOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;  // 订单号
    
    @Column(name = "user_id", nullable = false)
    private Long userId;  // 用户ID
    
    @Column(name = "package_code", nullable = false, length = 20)
    private String packageCode;  // 套餐代码
    
    @Column(name = "package_name", nullable = false, length = 50)
    private String packageName;  // 套餐名称
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // 单价
    
    @Column(name = "quantity")
    private Integer quantity = 1;  // 数量
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;  // 总金额
    
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;  // 支付方式: alipay/wechat
    
    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "PENDING";  // 支付状态
    
    @Column(name = "trade_no", length = 64)
    private String tradeNo;  // 第三方交易号
    
    @Column(name = "paid_time")
    private LocalDateTime paidTime;  // 支付时间
    
    @Column(name = "callback_url", length = 500)
    private String callbackUrl;  // 回调URL
    
    @Column(name = "callback_raw", columnDefinition = "TEXT")
    private String callbackRaw;  // 回调原始数据
    
    @Column(name = "callback_verified")
    private Boolean callbackVerified = false;  // 回调是否已验证
    
    @Column(name = "attach", length = 500)
    private String attach;  // 附加数据
    
    @Column(name = "client_ip", length = 50)
    private String clientIp;  // 客户端IP
    
    @Column(name = "device_info", length = 200)
    private String deviceInfo;  // 设备信息
    
    @Column(name = "error_code", length = 50)
    private String errorCode;  // 错误码
    
    @Column(name = "error_msg", length = 200)
    private String errorMsg;  // 错误信息
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;  // 订单过期时间
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 支付状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_CLOSED = "CLOSED";
    
    // 支付方式常量
    public static final String METHOD_ALIPAY = "alipay";
    public static final String METHOD_WECHAT = "wechat";
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    
    public LocalDateTime getPaidTime() { return paidTime; }
    public void setPaidTime(LocalDateTime paidTime) { this.paidTime = paidTime; }
    
    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
    
    public String getCallbackRaw() { return callbackRaw; }
    public void setCallbackRaw(String callbackRaw) { this.callbackRaw = callbackRaw; }
    
    public Boolean getCallbackVerified() { return callbackVerified; }
    public void setCallbackVerified(Boolean callbackVerified) { this.callbackVerified = callbackVerified; }
    
    public String getAttach() { return attach; }
    public void setAttach(String attach) { this.attach = attach; }
    
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 辅助方法
    public boolean isPaid() {
        return STATUS_SUCCESS.equals(paymentStatus);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
