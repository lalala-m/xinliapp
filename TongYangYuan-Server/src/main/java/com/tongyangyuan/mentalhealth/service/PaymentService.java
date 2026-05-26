package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.dto.CreateOrderRequest;
import com.tongyangyuan.mentalhealth.dto.CreateOrderResponse;
import com.tongyangyuan.mentalhealth.dto.OrderStatusResponse;
import com.tongyangyuan.mentalhealth.entity.MembershipPackage;
import com.tongyangyuan.mentalhealth.entity.MembershipRecord;
import com.tongyangyuan.mentalhealth.entity.PaymentOrder;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.MembershipPackageRepository;
import com.tongyangyuan.mentalhealth.repository.MembershipRecordRepository;
import com.tongyangyuan.mentalhealth.repository.PaymentOrderRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 支付服务
 */
@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentOrderRepository paymentOrderRepository;
    
    @Autowired
    private MembershipRecordRepository membershipRecordRepository;
    
    @Autowired
    private MembershipPackageRepository membershipPackageRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    // TODO: 实际项目中需要从配置或数据库获取
    // 微信支付配置
    private static final String WECHAT_APP_ID = "";  // 微信开放平台应用APPID
    private static final String WECHAT_MCH_ID = "";   // 微信商户号
    private static final String WECHAT_API_KEY = "";  // 微信支付API密钥
    
    // 支付宝配置
    private static final String ALIPAY_APP_ID = "";       // 支付宝应用APPID
    private static final String ALIPAY_PRIVATE_KEY = "";  // 支付宝私钥
    private static final String ALIPAY_PUBLIC_KEY = "";   // 支付宝公钥
    
    // 订单过期时间（分钟）
    private static final int ORDER_EXPIRE_MINUTES = 30;
    
    /**
     * 创建支付订单
     */
    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request) {
        CreateOrderResponse response = new CreateOrderResponse();
        
        try {
            // 1. 验证套餐
            MembershipPackage pkg = membershipPackageRepository.findByCode(request.getPackageCode())
                    .orElse(null);
            
            if (pkg == null || !pkg.getIsActive()) {
                response.setSuccess(false);
                response.setMessage("套餐不存在或已下架");
                return response;
            }
            
            // 2. 生成订单号
            String orderNo = generateOrderNo();
            
            // 3. 创建订单
            PaymentOrder order = new PaymentOrder();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setPackageCode(pkg.getCode());
            order.setPackageName(pkg.getName());
            order.setPrice(pkg.getPrice());
            order.setQuantity(1);
            order.setTotalAmount(pkg.getPrice());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setPaymentStatus(PaymentOrder.STATUS_PENDING);
            order.setClientIp(request.getClientIp());
            order.setDeviceInfo(request.getDeviceInfo());
            order.setExpiresAt(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));
            
            paymentOrderRepository.save(order);
            
            // 4. 根据支付方式生成支付参数
            String payParams = generatePayParams(order, request.getPaymentMethod());
            
            response.setSuccess(true);
            response.setMessage("订单创建成功");
            response.setOrderNo(orderNo);
            response.setPaymentMethod(request.getPaymentMethod());
            response.setTotalAmount(order.getTotalAmount());
            response.setPayParams(payParams);
            response.setExpiresAt(order.getExpiresAt());
            
            logger.info("创建订单成功: orderNo={}, userId={}, package={}", 
                    orderNo, userId, request.getPackageCode());
            
        } catch (Exception e) {
            logger.error("创建订单失败", e);
            response.setSuccess(false);
            response.setMessage("创建订单失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 查询订单状态
     */
    public OrderStatusResponse getOrderStatus(String orderNo) {
        OrderStatusResponse response = new OrderStatusResponse();
        
        Optional<PaymentOrder> orderOpt = paymentOrderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            response.setSuccess(false);
            response.setMessage("订单不存在");
            return response;
        }
        
        PaymentOrder order = orderOpt.get();
        
        response.setSuccess(true);
        response.setMessage("查询成功");
        response.setOrderNo(order.getOrderNo());
        response.setPackageCode(order.getPackageCode());
        response.setPackageName(order.getPackageName());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaidTime(order.getPaidTime());
        response.setCreatedAt(order.getCreatedAt());
        response.setExpiresAt(order.getExpiresAt());
        
        return response;
    }
    
    /**
     * 支付成功回调处理
     */
    @Transactional
    public boolean handlePaymentSuccess(String orderNo, String tradeNo) {
        try {
            Optional<PaymentOrder> orderOpt = paymentOrderRepository.findByOrderNo(orderNo);
            if (!orderOpt.isPresent()) {
                logger.error("支付回调处理失败: 订单不存在, orderNo={}", orderNo);
                return false;
            }
            
            PaymentOrder order = orderOpt.get();
            
            // 检查订单状态
            if (order.isPaid()) {
                logger.info("订单已支付, orderNo={}", orderNo);
                return true;
            }
            
            if (order.isExpired()) {
                // 订单已过期，更新状态
                order.setPaymentStatus(PaymentOrder.STATUS_CLOSED);
                order.setErrorCode("ORDER_EXPIRED");
                order.setErrorMsg("订单已过期");
                paymentOrderRepository.save(order);
                logger.warn("订单已过期, orderNo={}", orderNo);
                return false;
            }
            
            // 更新订单状态
            order.setPaymentStatus(PaymentOrder.STATUS_SUCCESS);
            order.setTradeNo(tradeNo);
            order.setPaidTime(LocalDateTime.now());
            paymentOrderRepository.save(order);
            
            // 判断是否为钱包充值订单
            if (walletService.isWalletRechargePackage(order.getPackageCode())) {
                // 钱包充值：增加余额
                BigDecimal rechargeAmount = walletService.getRechargeTotalAmount(order.getPackageCode());
                if (rechargeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    walletService.recharge(order.getUserId(), rechargeAmount, order.getOrderNo(), 
                            "充值套餐: " + order.getPackageName());
                    logger.info("钱包充值成功: orderNo={}, amount={}", orderNo, rechargeAmount);
                }
            } else {
                // 会员套餐：创建会员权益记录
                createMembershipRecord(order);
            }
            
            logger.info("支付成功处理完成: orderNo={}, tradeNo={}", orderNo, tradeNo);
            return true;
            
        } catch (Exception e) {
            logger.error("支付回调处理异常", e);
            return false;
        }
    }
    
    /**
     * 创建会员权益记录
     */
    @Transactional
    public void createMembershipRecord(PaymentOrder order) {
        try {
            MembershipPackage pkg = membershipPackageRepository.findByCode(order.getPackageCode())
                    .orElse(null);
            
            if (pkg == null) {
                logger.error("套餐不存在, packageCode={}", order.getPackageCode());
                return;
            }
            
            // 检查用户是否已有会员权益
            Optional<MembershipRecord> existingOpt = membershipRecordRepository
                    .findTopByUserIdAndStatusOrderByEndTimeDesc(order.getUserId(), MembershipRecord.STATUS_ACTIVE);
            
            LocalDateTime startTime;
            LocalDateTime endTime;
            
            if (existingOpt.isPresent() && existingOpt.get().isActive()) {
                // 叠加时间
                MembershipRecord existing = existingOpt.get();
                startTime = existing.getEndTime();
                endTime = startTime.plusDays(pkg.getDays());
            } else {
                // 从当前时间开始
                startTime = LocalDateTime.now();
                endTime = startTime.plusDays(pkg.getDays());
            }
            
            MembershipRecord record = new MembershipRecord();
            record.setUserId(order.getUserId());
            record.setPackageCode(pkg.getCode());
            record.setPackageName(pkg.getName());
            record.setStartTime(startTime);
            record.setEndTime(endTime);
            record.setDays(pkg.getDays());
            record.setOrderId(order.getId());
            record.setOrderNo(order.getOrderNo());
            record.setSource(MembershipRecord.SOURCE_PURCHASE);
            record.setStatus(MembershipRecord.STATUS_ACTIVE);
            
            membershipRecordRepository.save(record);
            
            // 同步更新 users 表的 is_vip 和 vip_expire_time，确保前端能正确读取会员状态
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user != null) {
                user.setIsVip(true);
                user.setVipExpireTime(endTime);
                userRepository.save(user);
                logger.info("同步更新用户VIP状态: userId={}, vipExpireTime={}", 
                        order.getUserId(), endTime);
            }
            
            logger.info("创建会员权益记录: userId={}, package={}, endTime={}", 
                    order.getUserId(), pkg.getCode(), endTime);
            
        } catch (Exception e) {
            logger.error("创建会员权益记录失败", e);
        }
    }
    
    /**
     * 查询用户会员状态
     */
    public MembershipStatus getMembershipStatus(Long userId) {
        MembershipStatus status = new MembershipStatus();
        status.setUserId(userId);
        
        Optional<MembershipRecord> recordOpt = membershipRecordRepository
                .findTopByUserIdAndStatusOrderByEndTimeDesc(userId, MembershipRecord.STATUS_ACTIVE);
        
        if (recordOpt.isPresent() && recordOpt.get().isActive()) {
            MembershipRecord record = recordOpt.get();
            status.setIsPaid(true);
            status.setPackageCode(record.getPackageCode());
            status.setPackageName(record.getPackageName());
            status.setStartTime(record.getStartTime());
            status.setEndTime(record.getEndTime());
            status.setRemainingDays(record.getRemainingDays());
        } else {
            status.setIsPaid(false);
        }
        
        // 获取所有可用套餐
        List<MembershipPackage> packages = membershipPackageRepository.findByIsActiveTrueOrderBySortOrderAsc();
        status.setPackages(packages);
        
        return status;
    }
    
    /**
     * 获取用户订单列表
     */
    public List<PaymentOrder> getUserOrders(Long userId) {
        return paymentOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TYY" + timestamp + uuid;
    }
    
    /**
     * 生成支付参数
     * TODO: 实际集成时需要调用微信/支付宝 SDK 生成
     */
    private String generatePayParams(PaymentOrder order, String paymentMethod) {
        if (PaymentOrder.METHOD_WECHAT.equals(paymentMethod)) {
            return generateWeChatPayParams(order);
        } else if (PaymentOrder.METHOD_ALIPAY.equals(paymentMethod)) {
            return generateAlipayParams(order);
        }
        return "";
    }
    
    /**
     * 生成微信支付参数
     * TODO: 实际集成时使用微信支付 SDK
     */
    private String generateWeChatPayParams(PaymentOrder order) {
        // 实际集成时，需要：
        // 1. 调用微信统一下单 API
        // 2. 生成调起微信支付的参数
        // 返回格式应该是调起支付的 JSON 参数
        
        // 示例返回值（实际使用时替换为真实逻辑）
        return String.format(
            "{\"appid\":\"%s\",\"partnerid\":\"%s\",\"prepayid\":\"%s\",\"package\":\"Sign=WXPay\",\"noncestr\":\"%s\",\"timestamp\":%d,\"sign\":\"%s\"}",
            WECHAT_APP_ID,
            WECHAT_MCH_ID,
            "PRE" + order.getOrderNo(),  // 预下单号
            UUID.randomUUID().toString().replace("-", ""),
            System.currentTimeMillis() / 1000,
            "SIGN_PLACEHOLDER"  // 实际需要计算签名
        );
    }
    
    /**
     * 生成支付宝支付参数
     * TODO: 实际集成时使用支付宝 SDK
     */
    private String generateAlipayParams(PaymentOrder order) {
        // 实际集成时，需要：
        // 1. 构建支付宝订单信息
        // 2. 使用 RSA2 签名
        // 3. 返回调起支付的参数字符串
        
        // 示例返回值（实际使用时替换为真实逻辑）
        return String.format(
            "{\"app_id\":\"%s\",\"method\":\"alipay.trade.app.pay\",\"biz_content\":\"{\\\"out_trade_no\\\":\\\"%s\\\",\\\"total_amount\\\":\\\"%.2f\\\",\\\"subject\\\":\\\"%s\\\"}\",\"sign_type\":\"RSA2\",\"timestamp\":\"%s\"}",
            ALIPAY_APP_ID,
            order.getOrderNo(),
            order.getTotalAmount(),
            order.getPackageName(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * 会员状态内部类
     */
    public static class MembershipStatus {
        private Long userId;
        private boolean isPaid;
        private String packageCode;
        private String packageName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long remainingDays;
        private List<MembershipPackage> packages;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public boolean isPaid() { return isPaid; }
        public void setIsPaid(boolean isPaid) { this.isPaid = isPaid; }
        
        public String getPackageCode() { return packageCode; }
        public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
        
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public long getRemainingDays() { return remainingDays; }
        public void setRemainingDays(long remainingDays) { this.remainingDays = remainingDays; }
        
        public List<MembershipPackage> getPackages() { return packages; }
        public void setPackages(List<MembershipPackage> packages) { this.packages = packages; }
    }
}
