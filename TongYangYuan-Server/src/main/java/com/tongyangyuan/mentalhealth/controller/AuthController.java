package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.dto.LoginRequest;
import com.tongyangyuan.mentalhealth.dto.RegisterRequest;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request.getPhone(), request.getPassword());
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<User> register(
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String userType,
            @RequestParam(required = false) String nickname) {
        try {
            User.UserType type = User.UserType.valueOf(userType.toUpperCase());
            User user = authService.register(phone, password, type, nickname);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/register/parent")
    public ApiResponse<Map<String, Object>> registerParent(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> result = authService.registerParent(request);
            return ApiResponse.success("家长注册成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/register/consultant")
    public ApiResponse<Map<String, Object>> registerConsultant(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> result = authService.registerConsultant(request);
            return ApiResponse.success("咨询师注册成功，请等待审核", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login/code")
    public ApiResponse<Map<String, Object>> loginWithCode(@RequestParam String phone, @RequestParam String code) {
        try {
            Map<String, Object> result = authService.loginWithCode(phone, code);
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 临时端点：重置所有测试账号的密码为 "123456"
    @PostMapping("/reset-passwords")
    public ApiResponse<String> resetPasswords() {
        try {
            authService.resetAllTestPasswords();
            return ApiResponse.success("所有测试账号密码已重置为 123456", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
