package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo; // 订单号
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // 购买用户ID
    
    @Column(name = "package_id")
    private Long packageId; // 套餐ID（可为空）
    
    @Column(name = "package_name", length = 200)
    private String packageName; // 套餐名称
    
    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType; // ORDER_TYPE_MEMBER: 会员订单, ORDER_TYPE_RECHARGE: 充值订单, ORDER_TYPE_PACKAGE: 套餐订单
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice; // 原价
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO; // 优惠金额
    
    @Column(name = "actual_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualPrice; // 实付金额
    
    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // ALIPAY, WECHAT, BALANCE
    
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus; // PENDING, PAID, REFUNDED, CANCELLED, EXPIRED
    
    @Column(name = "payment_time")
    private LocalDateTime paymentTime; // 支付时间
    
    @Column(name = "transaction_id", length = 128)
    private String transactionId; // 第三方交易流水号
    
    @Column(name = "vip_valid_days")
    private Integer vipValidDays; // VIP有效天数
    
    @Column(name = "vip_start_time")
    private LocalDateTime vipStartTime; // VIP开始时间
    
    @Column(name = "vip_expire_time")
    private LocalDateTime vipExpireTime; // VIP过期时间
    
    @Column(name = "client_ip", length = 50)
    private String clientIp; // 客户端IP
    
    @Column(name = "remark", length = 500)
    private String remark; // 备注
    
    @Column(name = "gmt_create", updatable = false)
    private LocalDateTime gmtCreate;
    
    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;
    
    @PrePersist
    protected void onCreate() {
        gmtCreate = LocalDateTime.now();
        gmtModified = LocalDateTime.now();
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        gmtModified = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getPackageId() {
        return packageId;
    }
    
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getActualPrice() {
        return actualPrice;
    }
    
    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Integer getVipValidDays() {
        return vipValidDays;
    }
    
    public void setVipValidDays(Integer vipValidDays) {
        this.vipValidDays = vipValidDays;
    }
    
    public LocalDateTime getVipStartTime() {
        return vipStartTime;
    }
    
    public void setVipStartTime(LocalDateTime vipStartTime) {
        this.vipStartTime = vipStartTime;
    }
    
    public LocalDateTime getVipExpireTime() {
        return vipExpireTime;
    }
    
    public void setVipExpireTime(LocalDateTime vipExpireTime) {
        this.vipExpireTime = vipExpireTime;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }
    
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
    
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }
    
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }
}
