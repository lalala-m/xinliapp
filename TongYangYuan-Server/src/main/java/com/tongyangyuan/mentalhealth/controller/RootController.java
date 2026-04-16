package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RootController {

    @GetMapping
    public ApiResponse<Map<String, Object>> welcome() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "童康源心理健康服务平台");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("endpoints", Map.of(
            "auth", "/api/auth",
            "consultants", "/api/consultants",
            "appointments", "/api/appointments",
            "messages", "/api/messages",
            "children", "/api/children"
        ));
        return ApiResponse.success("欢迎使用童康源心理健康服务平台 API", info);
    }
}
