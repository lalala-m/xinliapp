package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 会员权益记录实体
 */
@Entity
@Table(name = "membership_records")
public class MembershipRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;  // 用户ID
    
    @Column(name = "package_code", nullable = false, length = 20)
    private String packageCode;  // 套餐代码
    
    @Column(name = "package_name", nullable = false, length = 50)
    private String packageName;  // 套餐名称
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;  // 开始时间
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;  // 结束时间
    
    @Column(name = "days", nullable = false)
    private Integer days;  // 有效期天数
    
    @Column(name = "order_id")
    private Long orderId;  // 来源订单ID
    
    @Column(name = "order_no", length = 64)
    private String orderNo;  // 来源订单号
    
    @Column(name = "source", length = 20)
    private String source = "PURCHASE";  // 来源
    
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";  // 状态
    
    @Column(name = "auto_renew")
    private Boolean autoRenew = false;  // 是否自动续费
    
    @Column(name = "remark", length = 200)
    private String remark;  // 备注
    
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
    
    // 状态常量
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    // 来源常量
    public static final String SOURCE_PURCHASE = "PURCHASE";
    public static final String SOURCE_ADMIN = "ADMIN";
    public static final String SOURCE_GIFT = "GIFT";
    public static final String SOURCE_TRIAL = "TRIAL";
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 辅助方法
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status) && LocalDateTime.now().isBefore(endTime);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }
    
    public long getRemainingDays() {
        if (isExpired()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endTime);
    }
}
