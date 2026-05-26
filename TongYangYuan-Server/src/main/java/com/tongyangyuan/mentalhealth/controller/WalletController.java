package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.entity.RechargePackage;
import com.tongyangyuan.mentalhealth.entity.UserWallet;
import com.tongyangyuan.mentalhealth.entity.WalletTransaction;
import com.tongyangyuan.mentalhealth.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private WalletService walletService;

    /**
     * 获取钱包余额
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return unauthorizedResponse();
        }

        UserWallet wallet = walletService.getOrCreateWallet(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("balance", wallet.getBalance());
        data.put("totalRecharged", wallet.getTotalRecharged());
        data.put("totalConsumed", wallet.getTotalConsumed());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "50") int limit) {
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return unauthorizedResponse();
        }

        List<WalletTransaction> transactions = walletService.getTransactions(userId, limit);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", transactions);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取充值套餐列表
     */
    @GetMapping("/recharge-packages")
    public ResponseEntity<?> getRechargePackages() {
        List<RechargePackage> packages = walletService.getRechargePackages();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", packages);
        return ResponseEntity.ok(response);
    }

    /**
     * 使用钱包支付预约
     */
    @PostMapping("/pay-appointment")
    public ResponseEntity<?> payAppointment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> request) {
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return unauthorizedResponse();
        }

        Long appointmentId = request.get("appointmentId") instanceof Number
                ? ((Number) request.get("appointmentId")).longValue()
                : null;
        BigDecimal amount = request.get("amount") instanceof Number
                ? new BigDecimal(request.get("amount").toString())
                : null;
        String description = request.get("description") != null ? request.get("description").toString() : "预约支付";

        if (appointmentId == null || amount == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: 需要appointmentId和amount");
            return ResponseEntity.badRequest().body(response);
        }

        // 检查余额
        if (!walletService.hasEnoughBalance(userId, amount)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "余额不足");
            response.put("balance", walletService.getBalance(userId));
            response.put("required", amount);
            return ResponseEntity.ok(response);
        }

        boolean success = walletService.consume(userId, amount, appointmentId, description);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "支付成功");
            response.put("balance", walletService.getBalance(userId));
        } else {
            response.put("success", false);
            response.put("message", "支付失败");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 检查余额是否充足
     */
    @GetMapping("/check-balance")
    public ResponseEntity<?> checkBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam BigDecimal amount) {
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return unauthorizedResponse();
        }

        boolean enough = walletService.hasEnoughBalance(userId, amount);
        BigDecimal balance = walletService.getBalance(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("enough", enough);
        data.put("balance", balance);
        data.put("required", amount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    // ========== 私有方法 ==========

    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (token.startsWith("user_")) {
            try {
                return Long.parseLong(token.substring(5));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return 1L; // 临时处理，实际应解析JWT
    }

    private ResponseEntity<?> unauthorizedResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "用户未登录");
        return ResponseEntity.status(401).body(response);
    }
}
