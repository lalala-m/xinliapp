package com.tongyangyuan.mentalhealth;

import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MentalHealthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentalHealthApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin user exists
            if (!userRepository.existsByPhone("13800000000")) {
                User admin = new User();
                admin.setPhone("13800000000");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNickname("Admin");
                admin.setUserType(User.UserType.ADMIN);
                admin.setStatus(User.UserStatus.ACTIVE);
                userRepository.save(admin);
                System.out.println("Default admin user created: 13800000000 / admin123");
            }
        };
    }
}
