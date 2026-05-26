package com.tongyangyuan.mentalhealth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 为 password123 生成哈希
        String hash = encoder.encode("password123");
        System.out.println("Hash for 'password123': " + hash);
    }
}
