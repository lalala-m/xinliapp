package com.tongyangyuan.mentalhealth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1c.e49iP1./F.";
        
        boolean matches = encoder.matches(password, hash);
        System.out.println("Password matches: " + matches);
        
        if (!matches) {
            String newHash = encoder.encode(password);
            System.out.println("New hash for '123456': " + newHash);
        }
    }
}





