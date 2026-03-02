package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.service.ConsultantCertificationService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultant/certification")
public class ConsultantCertificationController {

    @Autowired
    private ConsultantCertificationService certificationService;

    @Autowired
    private JwtUtil jwtUtil;

    // 简单的文件上传保存逻辑，实际生产应上传至OSS
    private final Path fileStorageLocation = Paths.get("uploads/certificates").toAbsolutePath().normalize();

    public ConsultantCertificationController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForYellowV(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("specialty") String specialty) {
        
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // 构建访问URL (假设静态资源映射已配置 /uploads/**)
            String fileUrl = "/uploads/certificates/" + fileName;
            
            Consultant consultant = certificationService.applyForYellowV(userId, fileUrl, specialty);
            return ResponseEntity.ok(Map.of("message", "申请已提交", "data", consultant));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payDeposit(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, BigDecimal> payload) {
        
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        BigDecimal amount = payload.get("amount");
        
        try {
            Consultant consultant = certificationService.payDeposit(userId, amount);
            return ResponseEntity.ok(Map.of("message", "保证金缴纳成功", "data", consultant));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Map<String, Object> status = certificationService.getCertificationStatus(userId);
        return ResponseEntity.ok(status);
    }
}
