package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 临时密码重置接口 - 仅用于开发环境
     * 使用方法: POST /api/admin/reset-password?phone=13800000003&newPassword=123456
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam String phone,
            @RequestParam(defaultValue = "123456") String newPassword) {
        
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + phone));

        // 使用BCrypt重新加密密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码已重置为: " + newPassword);
        result.put("phone", phone);
        
        return ResponseEntity.ok(result);
    }
}

