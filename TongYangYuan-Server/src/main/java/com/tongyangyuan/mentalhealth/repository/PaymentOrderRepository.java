package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付订单 Repository
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    
    Optional<PaymentOrder> findByOrderNo(String orderNo);
    
    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<PaymentOrder> findByUserIdAndPaymentStatus(Long userId, String paymentStatus);
    
    boolean existsByOrderNo(String orderNo);
}
