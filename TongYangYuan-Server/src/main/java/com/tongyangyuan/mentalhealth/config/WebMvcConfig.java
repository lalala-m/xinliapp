package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册管理员权限拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")  // 拦截所有/admin/**路径
                .excludePathPatterns("/auth/**");  // 排除登录接口
    }

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        // 🔧 使用相对路径 file:./uploads/，这样 uploads 目录跟随应用部署位置
        // 无论部署到哪个服务器，只要 uploads/ 在应用运行目录下就能访问
        String uploadPath = "file:./uploads/";
        System.out.println("[WebMvcConfig] Upload resource path (relative): " + uploadPath);
        
        // 映射 /api/uploads/** 到 file:./uploads/
        // 因为 server.servlet.context-path=/api，所以这里用 /uploads/**
        // Spring 会自动加上 context-path 前缀
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600);
    }
}
