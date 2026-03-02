package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.TestQuestion;
import com.tongyangyuan.mentalhealth.entity.UserTestRecord;
import com.tongyangyuan.mentalhealth.service.TestService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取测试题
     */
    @GetMapping("/packages/{packageId}/questions")
    public ResponseEntity<ApiResponse<List<TestQuestion>>> getQuestions(
            @PathVariable Long packageId) {
        List<TestQuestion> questions = testService.getTestQuestions(packageId);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }
    
    /**
     * 提交测试答案
     */
    @PostMapping("/packages/{packageId}/submit")
    public ResponseEntity<ApiResponse<UserTestRecord>> submitTest(
            @PathVariable Long packageId,
            @RequestBody com.tongyangyuan.mentalhealth.dto.TestSubmissionDTO request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        UserTestRecord record = testService.submitTest(userId, packageId, request.getAnswers(), request.getTimeSpent());
        return ResponseEntity.ok(ApiResponse.success(record));
    }
    
    /**
     * 获取测试记录
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<Page<UserTestRecord>>> getTestRecords(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Pageable pageable = PageRequest.of(page, size);
        Page<UserTestRecord> records = testService.getUserTestRecords(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
