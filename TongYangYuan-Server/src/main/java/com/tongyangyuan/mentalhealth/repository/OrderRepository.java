package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 通过订单号查找
     */
    Optional<Order> findByOrderNo(String orderNo);
    
    /**
     * 通过用户ID查找订单列表
     */
    Page<Order> findByUserIdOrderByGmtCreateDesc(Long userId, Pageable pageable);
    
    /**
     * 通过用户ID和订单状态查找
     */
    Page<Order> findByUserIdAndPaymentStatusOrderByGmtCreateDesc(
            Long userId, String paymentStatus, Pageable pageable);
    
    /**
     * 通过用户ID查找已支付订单
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.paymentStatus = 'PAID' ORDER BY o.paymentTime DESC")
    List<Order> findPaidOrdersByUserId(@Param("userId") Long userId);
    
    /**
     * 查找未支付的订单（超时自动取消）
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PENDING' AND o.gmtCreate < :expireTime")
    List<Order> findExpiredPendingOrders(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 检查用户是否有有效VIP订单
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.userId = :userId AND o.paymentStatus = 'PAID' " +
           "AND o.orderType = 'ORDER_TYPE_MEMBER' AND o.vipExpireTime > :now")
    boolean hasValidVipOrder(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * 通过交易ID查找
     */
    Optional<Order> findByTransactionId(String transactionId);
}
