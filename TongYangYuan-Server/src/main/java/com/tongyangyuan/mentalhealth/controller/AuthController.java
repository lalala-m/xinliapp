package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.dto.LoginRequest;
import com.tongyangyuan.mentalhealth.dto.RegisterRequest;
import com.tongyangyuan.mentalhealth.dto.WeChatLoginRequest;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.service.AuthService;
import com.tongyangyuan.mentalhealth.service.ConsultantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ConsultantService consultantService;

    public AuthController(AuthService authService, ConsultantService consultantService) {
        this.authService = authService;
        this.consultantService = consultantService;
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

    /**
     * 咨询师登录（手机号+密码）
     * 返回：token + userInfo + consultantInfo（含头像）
     */
    @PostMapping("/login/consultant")
    public ApiResponse<Map<String, Object>> consultantLogin(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request.getPhone(), request.getPassword());
            // 检查是否为咨询师类型
            String userType = (String) result.get("userType");
            if (!"CONSULTANT".equals(userType)) {
                return ApiResponse.error("该账号不是咨询师账号，请使用普通用户登录");
            }
            // 附加咨询师详细信息（含头像）
            Long userId = ((Number) result.get("userId")).longValue();
            try {
                Consultant consultant = consultantService.getConsultantByUserId(userId);
                if (consultant != null) {
                    result.put("consultant", consultant);
                }
            } catch (Exception ignored) {
                // 找不到咨询师信息不影响登录
            }
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 咨询师快捷登录（手机号+验证码）
     */
    @PostMapping("/login/consultant/code")
    public ApiResponse<Map<String, Object>> consultantLoginWithCode(
            @RequestParam String phone,
            @RequestParam String code) {
        try {
            Map<String, Object> result = authService.loginWithCode(phone, code);
            String userType = (String) result.get("userType");
            if (!"CONSULTANT".equals(userType)) {
                return ApiResponse.error("该账号不是咨询师账号");
            }
            Long userId = ((Number) result.get("userId")).longValue();
            try {
                Consultant consultant = consultantService.getConsultantByUserId(userId);
                if (consultant != null) {
                    result.put("consultant", consultant);
                }
            } catch (Exception ignored) {}
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

    @PostMapping("/sendCode")
    public ApiResponse<Void> sendCode(@RequestParam String phone) {
        try {
            authService.sendVerificationCode(phone);
            return ApiResponse.success("验证码已发送", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login/password")
    public ApiResponse<Map<String, Object>> loginWithPassword(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String phone,
            @RequestParam String password) {
        String loginKey = (account != null && !account.isEmpty()) ? account : phone;
        try {
            Map<String, Object> result = authService.login(loginKey, password);
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login/wechat")
    public ApiResponse<Map<String, Object>> loginWithWechat(@RequestBody WeChatLoginRequest request) {
        try {
            Map<String, Object> result = authService.loginWithWechat(request);
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
