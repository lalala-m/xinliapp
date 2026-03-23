package com.tongyangyuan.mentalhealth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 临时工具：生成密码 "123456" 的正确 BCrypt 哈希值
 * 运行此类的 main 方法可以生成新的哈希值
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        
        // 生成新的哈希值
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Generated Hash: " + hash);
        
        // 验证
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification Result: " + matches);
        
        // 也测试一下旧的哈希值
        String oldHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1c.e49iP1./F.";
        boolean oldMatches = encoder.matches(password, oldHash);
        System.out.println("Old Hash Verification: " + oldMatches);
        
        String anotherOldHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean anotherMatches = encoder.matches(password, anotherOldHash);
        System.out.println("Another Old Hash Verification: " + anotherMatches);
    }
}
