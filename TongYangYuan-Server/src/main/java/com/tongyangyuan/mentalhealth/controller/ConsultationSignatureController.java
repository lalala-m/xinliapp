package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.ConsultationSignature;
import com.tongyangyuan.mentalhealth.service.ConsultationSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 咨询签名控制器
 */
@RestController
@RequestMapping("/consultation-signatures")
public class ConsultationSignatureController {
    
    @Autowired
    private ConsultationSignatureService signatureService;
    
    /**
     * 提交签名
     * POST /api/consultation/signatures/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitSignature(@RequestBody Map<String, Object> request) {
        try {
            // 参数校验
            if (request.get("recordId") == null) {
                return ResponseEntity.ok(ApiResponse.error("缺少recordId参数"));
            }
            if (request.get("signerType") == null) {
                return ResponseEntity.ok(ApiResponse.error("缺少signerType参数"));
            }
            if (request.get("signatureImage") == null) {
                return ResponseEntity.ok(ApiResponse.error("缺少signatureImage参数"));
            }
            
            Long recordId = Long.parseLong(request.get("recordId").toString());
            String signerType = request.get("signerType").toString();
            Long signerId = request.get("signerId") != null ? Long.parseLong(request.get("signerId").toString()) : null;
            String signerName = request.get("signerName") != null ? request.get("signerName").toString() : "家长";
            String signatureImage = request.get("signatureImage").toString();
            String verificationMediaUrl = request.get("verificationMediaUrl") != null ? request.get("verificationMediaUrl").toString() : null;
            String ipAddress = request.get("ipAddress") != null ? request.get("ipAddress").toString() : null;
            String clientInfo = request.get("clientInfo") != null ? request.get("clientInfo").toString() : null;
            
            ConsultationSignature signature = signatureService.submitSignature(
                    recordId, signerType, signerId, signerName, 
                    signatureImage, verificationMediaUrl, ipAddress, clientInfo);
            
            return ResponseEntity.ok(ApiResponse.success("签名提交成功", Map.of(
                "signatureId", signature.getId(),
                "signedAt", signature.getSignedAt()
            )));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("签名提交失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取签名字列表
     * GET /api/consultation/signatures/{recordId}
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<?> getSignatures(@PathVariable Long recordId) {
        try {
            List<ConsultationSignature> signatures = signatureService.getSignatures(recordId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", signatures);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取签名列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 获取签名状态
     * GET /api/consultation/signatures/status/{recordId}
     */
    @GetMapping("/status/{recordId}")
    public ResponseEntity<?> getSignatureStatus(@PathVariable Long recordId) {
        try {
            Map<String, Boolean> status = signatureService.getSignatureStatus(recordId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", status);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取签名状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 删除签名
     * DELETE /api/consultation/signatures/{signatureId}
     */
    @DeleteMapping("/{signatureId}")
    public ResponseEntity<?> deleteSignature(@PathVariable Long signatureId) {
        try {
            signatureService.deleteSignature(signatureId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "签名删除成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "签名删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}