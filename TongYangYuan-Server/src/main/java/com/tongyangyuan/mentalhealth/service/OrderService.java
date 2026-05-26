package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.MembershipRecord;
import com.tongyangyuan.mentalhealth.entity.Order;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.MembershipRecordRepository;
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
    
    @Autowired
    private MembershipRecordRepository membershipRecordRepository;
    
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
     * 同时更新 users 表和 membership_records 表，保持数据一致性
     */
    @Transactional
    public void activateVipForUser(Long userId, Integer validDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        LocalDateTime expireTime;
        
        // 检查是否已有活跃会员记录
        Optional<MembershipRecord> existingOpt = membershipRecordRepository
                .findTopByUserIdAndStatusOrderByEndTimeDesc(userId, MembershipRecord.STATUS_ACTIVE);
        
        if (existingOpt.isPresent() && existingOpt.get().isActive()) {
            // 累加时间
            MembershipRecord existing = existingOpt.get();
            startTime = existing.getEndTime();
            expireTime = startTime.plusDays(validDays);
        } else {
            // 从当前时间开始
            startTime = now;
            expireTime = now.plusDays(validDays);
        }
        
        // 1. 更新 users 表
        user.setIsVip(true);
        user.setVipExpireTime(expireTime);
        userRepository.save(user);
        
        // 2. 创建 membership_records 记录（确保新系统也能读取）
        MembershipRecord record = new MembershipRecord();
        record.setUserId(userId);
        record.setPackageCode("legacy_order");
        record.setPackageName("会员套餐");
        record.setStartTime(startTime);
        record.setEndTime(expireTime);
        record.setDays(validDays);
        record.setSource(MembershipRecord.SOURCE_PURCHASE);
        record.setStatus(MembershipRecord.STATUS_ACTIVE);
        membershipRecordRepository.save(record);
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
