package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.CreateOrderRequest;
import com.tongyangyuan.mentalhealth.dto.CreateOrderResponse;
import com.tongyangyuan.mentalhealth.dto.OrderStatusResponse;
import com.tongyangyuan.mentalhealth.entity.PaymentOrder;
import com.tongyangyuan.mentalhealth.repository.PaymentOrderRepository;
import com.tongyangyuan.mentalhealth.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付 Controller
 */
@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentOrderRepository paymentOrderRepository;
    
    /**
     * 创建支付订单
     */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        
        // 解析用户ID（从JWT token中获取）
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            CreateOrderResponse response = new CreateOrderResponse();
            response.setSuccess(false);
            response.setMessage("用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // 获取客户端IP
        String clientIp = getClientIp(httpRequest);
        request.setClientIp(clientIp);
        
        // 获取设备信息
        String deviceInfo = httpRequest.getHeader("User-Agent");
        request.setDeviceInfo(deviceInfo);
        
        CreateOrderResponse response = paymentService.createOrder(userId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查询订单状态
     */
    @GetMapping("/order-status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(
            @RequestParam String orderNo) {
        
        OrderStatusResponse response = paymentService.getOrderStatus(orderNo);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户会员状态
     */
    @GetMapping("/membership-status")
    public ResponseEntity<?> getMembershipStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        PaymentService.MembershipStatus status = paymentService.getMembershipStatus(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getUserOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        List<PaymentOrder> orders = paymentService.getUserOrders(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", orders);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取可用套餐列表
     */
    @GetMapping("/packages")
    public ResponseEntity<?> getPackages() {
        PaymentService.MembershipStatus status = paymentService.getMembershipStatus(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", status.getPackages());
        return ResponseEntity.ok(response);
    }
    
    // ========== 支付回调接口 ==========
    
    /**
     * 微信支付回调
     */
    @PostMapping("/notify/wechat")
    public ResponseEntity<String> wechatNotify(HttpServletRequest request) {
        try {
            // 获取回调数据
            Map<String, String> params = getRequestParams(request);
            String orderNo = params.get("out_trade_no");  // 商户订单号
            String transactionId = params.get("transaction_id");  // 微信订单号
            String resultCode = params.get("result_code");  // 业务结果
            
            logger.info("收到微信支付回调: orderNo={}, transactionId={}, resultCode={}", 
                    orderNo, transactionId, resultCode);
            
            // TODO: 验证签名
            // verifyWechatSign(params);
            
            if ("SUCCESS".equals(resultCode)) {
                boolean success = paymentService.handlePaymentSuccess(orderNo, transactionId);
                if (success) {
                    return ResponseEntity.ok("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
                }
            }
            
            return ResponseEntity.ok("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
            
        } catch (Exception e) {
            logger.error("微信支付回调处理异常", e);
            return ResponseEntity.ok("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
        }
    }
    
    /**
     * 支付宝回调
     */
    @PostMapping("/notify/alipay")
    public ResponseEntity<String> alipayNotify(HttpServletRequest request) {
        try {
            // 获取回调数据
            Map<String, String> params = getRequestParams(request);
            String orderNo = params.get("out_trade_no");  // 商户订单号
            String tradeNo = params.get("trade_no");  // 支付宝交易号
            String tradeStatus = params.get("trade_status");  // 交易状态
            
            logger.info("收到支付宝回调: orderNo={}, tradeNo={}, tradeStatus={}", 
                    orderNo, tradeNo, tradeStatus);
            
            // TODO: 验证签名
            // verifyAlipaySign(params);
            
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                boolean success = paymentService.handlePaymentSuccess(orderNo, tradeNo);
                if (success) {
                    return ResponseEntity.ok("success");
                }
            }
            
            return ResponseEntity.ok("fail");
            
        } catch (Exception e) {
            logger.error("支付宝回调处理异常", e);
            return ResponseEntity.ok("fail");
        }
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 从 Authorization Header 提取用户ID
     * TODO: 实际项目中应该解析JWT token
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        
        // TODO: 实际项目中应该解析JWT token获取用户ID
        // 这里简单处理：假设token格式为 "user_{userId}"
        if (token.startsWith("user_")) {
            try {
                return Long.parseLong(token.substring(5));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // 临时处理：从请求参数获取（仅用于测试）
        return 1L;  // TODO: 删除这行，实际使用JWT解析
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }
}
