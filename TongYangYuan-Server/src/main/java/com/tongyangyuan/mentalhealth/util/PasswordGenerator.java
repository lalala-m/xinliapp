package com.tongyangyuan.mentalhealth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 临时工具类：用于生成正确的 BCrypt 密码哈希值
 * 密码 "123456" 的正确哈希值
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // 验证
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
    }
}






