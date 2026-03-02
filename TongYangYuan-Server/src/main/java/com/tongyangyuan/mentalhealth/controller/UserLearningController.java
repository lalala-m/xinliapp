package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.UserLearningProgress;
import com.tongyangyuan.mentalhealth.service.UserLearningService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/learning")
public class UserLearningController {
    
    @Autowired
    private UserLearningService learningService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 更新视频观看进度
     */
    @PostMapping("/videos/{videoId}/progress")
    public ResponseEntity<ApiResponse<UserLearningProgress>> updateProgress(
            @PathVariable Long videoId,
            @RequestBody com.tongyangyuan.mentalhealth.dto.VideoProgressDTO request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        UserLearningProgress progress = learningService.updateVideoProgress(
            userId, videoId, request.getPackageId(), request.getWatchDuration(), request.getLastPosition());
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    /**
     * 提交验证题答案
     */
    @PostMapping("/videos/{videoId}/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitVerification(
            @PathVariable Long videoId,
            @RequestBody com.tongyangyuan.mentalhealth.dto.VerificationSubmissionDTO request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Map<String, Object> result = learningService.submitVerificationAnswer(userId, videoId, request.getAnswer());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 获取学习记录
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLearningRecords(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        List<Map<String, Object>> records = learningService.getUserLearningRecords(userId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
