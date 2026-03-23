package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.LearningPackage;
import com.tongyangyuan.mentalhealth.service.LearningPackageService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/learning/packages")
public class LearningPackageController {
    
    @Autowired
    private LearningPackageService learningPackageService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取所有学习包
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningPackage>>> getAllPackages() {
        List<LearningPackage> packages = learningPackageService.getAllActivePackages();
        return ResponseEntity.ok(ApiResponse.success(packages));
    }
    
    /**
     * 分页获取学习包
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<LearningPackage>>> getPackagesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningPackage> packages = learningPackageService.getPackages(pageable);
        return ResponseEntity.ok(ApiResponse.success(packages));
    }
    
    /**
     * 根据分类获取学习包
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<LearningPackage>>> getPackagesByCategory(
            @PathVariable String category) {
        List<LearningPackage> packages = learningPackageService.getPackagesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(packages));
    }
    
    /**
     * 获取学习包详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPackageDetail(
            @PathVariable Long id) {
        Map<String, Object> detail = learningPackageService.getPackageDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
    
    /**
     * 获取推荐学习包
     */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecommendedPackages(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        List<Map<String, Object>> packages = learningPackageService.getRecommendedPackages(userId);
        return ResponseEntity.ok(ApiResponse.success(packages));
    }
    
    /**
     * 开始学习包
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<String>> startLearningPackage(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        learningPackageService.startLearningPackage(userId, id);
        return ResponseEntity.ok(ApiResponse.success("开始学习成功"));
    }
    
    /**
     * 获取用户在学习包中的进度
     */
    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserPackageProgress(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Map<String, Object> progress = learningPackageService.getUserPackageProgress(userId, id);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        List<String> categories = learningPackageService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
