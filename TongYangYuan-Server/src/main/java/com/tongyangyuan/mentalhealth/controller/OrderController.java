package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Order;
import com.tongyangyuan.mentalhealth.service.OrderService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建订单
     * POST /orders
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = null;
            if (token != null && !token.isEmpty()) {
                userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            }
            
            if (userId == null) {
                return ApiResponse.error(401, "请先登录");
            }
            
            String clientIp = getClientIp(httpRequest);
            
            Order order = orderService.createOrder(
                    userId,
                    request.getOrderType(),
                    request.getPackageId(),
                    request.getPackageName(),
                    request.getOriginalPrice(),
                    request.getDiscountAmount(),
                    request.getActualPrice(),
                    request.getVipValidDays(),
                    clientIp
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("actualPrice", order.getActualPrice());
            result.put("paymentStatus", order.getPaymentStatus());
            result.put("expireTime", order.getGmtCreate().plusMinutes(30));
            
            return ApiResponse.success("订单创建成功", result);
            
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     * GET /orders/{orderNo}
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> getOrderDetail(
            @PathVariable String orderNo,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            Long userId = null;
            if (token != null && !token.isEmpty()) {
                userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            }
            
            Order order = orderService.getOrderByNo(orderNo)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            
            // 验证订单属于当前用户（如果已登录）
            if (userId != null && !order.getUserId().equals(userId)) {
                return ApiResponse.error(403, "无权访问该订单");
            }
            
            return ApiResponse.success(order);
            
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户订单列表
     * GET /orders/user?status=&page=0&size=10
     */
    @GetMapping("/user")
    public ApiResponse<Page<Order>> getUserOrders(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            if (token == null || token.isEmpty()) {
                return ApiResponse.error(401, "请先登录");
            }
            
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Order> orders = orderService.getUserOrders(userId, status, pageable);
            return ApiResponse.success(orders);
            
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 模拟支付（用于测试/演示）
     * POST /orders/{orderNo}/simulate-pay
     */
    @PostMapping("/{orderNo}/simulate-pay")
    public ApiResponse<Map<String, Object>> simulatePayment(
            @PathVariable String orderNo,
            @RequestBody SimulatePayRequest request) {
        
        try {
            Order order = orderService.simulatePaymentSuccess(orderNo, request.getPaymentMethod());
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", order.getOrderNo());
            result.put("paymentStatus", order.getPaymentStatus());
            result.put("paymentTime", order.getPaymentTime());
            result.put("isVip", order.getUserId() != null ? orderService.hasValidVip(order.getUserId()) : false);
            
            String message = "支付成功";
            if ("ORDER_TYPE_MEMBER".equals(order.getOrderType())) {
                message += "，您已成为会员";
            }
            
            return ApiResponse.success(message, result);
            
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 支付回调（供支付宝/微信等支付渠道回调）
     * POST /orders/callback
     */
    @PostMapping("/callback")
    public ApiResponse<Map<String, Object>> paymentCallback(
            @RequestBody PaymentCallbackRequest callback) {
        
        try {
            // 在实际生产环境中，需要验证签名等安全检查
            // 这里简化处理，直接处理回调
            Order order = orderService.handlePaymentCallback(
                    callback.getOrderNo(),
                    callback.getTransactionId(),
                    callback.getPaymentStatus()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", order.getOrderNo());
            result.put("paymentStatus", order.getPaymentStatus());
            
            return ApiResponse.success("回调处理成功", result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 取消订单
     * POST /orders/{orderNo}/cancel
     */
    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Order> cancelOrder(@PathVariable String orderNo) {
        try {
            Order order = orderService.cancelOrder(orderNo);
            return ApiResponse.success("订单已取消", order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    // ==================== 请求体类 ====================
    
    public static class CreateOrderRequest {
        private String orderType; // ORDER_TYPE_MEMBER, ORDER_TYPE_RECHARGE, ORDER_TYPE_PACKAGE
        private Long packageId;
        private String packageName;
        private BigDecimal originalPrice;
        private BigDecimal discountAmount;
        private BigDecimal actualPrice;
        private Integer vipValidDays;
        
        // Getters and Setters
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        public Long getPackageId() { return packageId; }
        public void setPackageId(Long packageId) { this.packageId = packageId; }
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
        public BigDecimal getActualPrice() { return actualPrice; }
        public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }
        public Integer getVipValidDays() { return vipValidDays; }
        public void setVipValidDays(Integer vipValidDays) { this.vipValidDays = vipValidDays; }
    }
    
    public static class SimulatePayRequest {
        private String paymentMethod; // ALIPAY, WECHAT
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
    
    public static class PaymentCallbackRequest {
        private String orderNo;
        private String transactionId;
        private String paymentStatus; // SUCCESS, FAILED
        
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }
}
