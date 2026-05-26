package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体
 */
@Entity
@Table(name = "user_wallets")
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_recharged", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRecharged = BigDecimal.ZERO;

    @Column(name = "total_consumed", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalConsumed = BigDecimal.ZERO;

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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getTotalRecharged() { return totalRecharged; }
    public void setTotalRecharged(BigDecimal totalRecharged) { this.totalRecharged = totalRecharged; }

    public BigDecimal getTotalConsumed() { return totalConsumed; }
    public void setTotalConsumed(BigDecimal totalConsumed) { this.totalConsumed = totalConsumed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * 增加余额（充值）
     */
    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalRecharged = this.totalRecharged.add(amount);
    }

    /**
     * 减少余额（消费）
     */
    public void subtractBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.totalConsumed = this.totalConsumed.add(amount);
    }

    /**
     * 检查余额是否充足
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
