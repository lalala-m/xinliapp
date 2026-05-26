package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录实体
 */
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    // 交易类型常量
    public static final String TYPE_RECHARGE = "RECHARGE";
    public static final String TYPE_CONSUME = "CONSUME";
    public static final String TYPE_REFUND = "REFUND";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_no", nullable = false, unique = true, length = 64)
    private String transactionNo;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "related_order_no", length = 64)
    private String relatedOrderNo;

    @Column(name = "related_appointment_id")
    private Long relatedAppointmentId;

    @Column(length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getRelatedOrderNo() { return relatedOrderNo; }
    public void setRelatedOrderNo(String relatedOrderNo) { this.relatedOrderNo = relatedOrderNo; }

    public Long getRelatedAppointmentId() { return relatedAppointmentId; }
    public void setRelatedAppointmentId(Long relatedAppointmentId) { this.relatedAppointmentId = relatedAppointmentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
