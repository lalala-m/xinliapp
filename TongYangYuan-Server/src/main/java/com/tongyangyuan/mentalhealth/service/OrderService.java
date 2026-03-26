package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Order;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.OrderRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 订单有效期（分钟）
    private static final int ORDER_EXPIRE_MINUTES = 30;
    
    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Long userId, String orderType, Long packageId, String packageName,
                            BigDecimal originalPrice, BigDecimal discountAmount, BigDecimal actualPrice,
                            Integer vipValidDays, String clientIp) {
        // 生成订单号
        String orderNo = generateOrderNo();
        
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setPackageId(packageId);
        order.setPackageName(packageName);
        order.setOriginalPrice(originalPrice);
        order.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        order.setActualPrice(actualPrice);
        order.setVipValidDays(vipValidDays);
        order.setPaymentStatus("PENDING");
        order.setClientIp(clientIp);
        
        return orderRepository.save(order);
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TYY" + timestamp + uuid;
    }
    
    /**
     * 获取订单详情
     */
    public Optional<Order> getOrderByNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }
    
    /**
     * 获取用户的订单列表
     */
    public Page<Order> getUserOrders(Long userId, String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByUserIdAndPaymentStatusOrderByGmtCreateDesc(userId, status, pageable);
        }
        return orderRepository.findByUserIdOrderByGmtCreateDesc(userId, pageable);
    }
    
    /**
     * 模拟支付成功回调（用于测试）
     */
    @Transactional
    public Order simulatePaymentSuccess(String orderNo, String paymentMethod) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));
        
        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new RuntimeException("订单状态不是待支付: " + order.getPaymentStatus());
        }
        
        // 更新订单状态
        order.setPaymentStatus("PAID");
        order.setPaymentTime(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        order.setTransactionId("SIM_" + System.currentTimeMillis());
        
        // 如果是VIP会员订单，更新用户VIP状态
        if ("ORDER_TYPE_MEMBER".equals(order.getOrderType())) {
            activateVipForUser(order.getUserId(), order.getVipValidDays());
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * 支付回调（真实支付渠道调用）
     */
    @Transactional
    public Order handlePaymentCallback(String orderNo, String transactionId, String paymentStatus) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));
        
        if ("SUCCESS".equals(paymentStatus)) {
            if (!"PENDING".equals(order.getPaymentStatus())) {
                throw new RuntimeException("订单状态不是待支付: " + order.getPaymentStatus());
            }
            
            order.setPaymentStatus("PAID");
            order.setPaymentTime(LocalDateTime.now());
            order.setTransactionId(transactionId);
            
            // 如果是VIP会员订单，更新用户VIP状态
            if ("ORDER_TYPE_MEMBER".equals(order.getOrderType())) {
                activateVipForUser(order.getUserId(), order.getVipValidDays());
            }
        } else if ("FAILED".equals(paymentStatus)) {
            order.setPaymentStatus("CANCELLED");
            order.setRemark("支付失败: " + transactionId);
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * 激活用户VIP
     */
    @Transactional
    public void activateVipForUser(Long userId, Integer validDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime;
        
        // 如果用户已有VIP且未过期，累加时间
        if (Boolean.TRUE.equals(user.getIsVip()) && user.getVipExpireTime() != null 
                && user.getVipExpireTime().isAfter(now)) {
            expireTime = user.getVipExpireTime().plusDays(validDays);
        } else {
            // 否则从现在开始计算
            expireTime = now.plusDays(validDays);
        }
        
        user.setIsVip(true);
        user.setVipExpireTime(expireTime);
        userRepository.save(user);
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public Order cancelOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));
        
        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new RuntimeException("订单状态不是待支付，无法取消");
        }
        
        order.setPaymentStatus("CANCELLED");
        return orderRepository.save(order);
    }
    
    /**
     * 处理超时未支付订单
     */
    @Transactional
    public void expirePendingOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(ORDER_EXPIRE_MINUTES);
        var expiredOrders = orderRepository.findExpiredPendingOrders(expireTime);
        
        for (Order order : expiredOrders) {
            order.setPaymentStatus("EXPIRED");
            order.setRemark("订单超时未支付，自动取消");
            orderRepository.save(order);
        }
    }
    
    /**
     * 检查用户是否有有效VIP
     */
    public boolean hasValidVip(Long userId) {
        return orderRepository.hasValidVipOrder(userId, LocalDateTime.now());
    }
}
