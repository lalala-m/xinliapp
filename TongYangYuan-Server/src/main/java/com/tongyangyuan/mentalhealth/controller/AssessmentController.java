package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.PsychologicalAssessment;
import com.tongyangyuan.mentalhealth.service.AssessmentService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {
    
    @Autowired
    private AssessmentService assessmentService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取评估报告列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PsychologicalAssessment>>> getAssessments(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Pageable pageable = PageRequest.of(page, size);
        Page<PsychologicalAssessment> assessments = assessmentService.getUserAssessments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }
    
    /**
     * 获取评估报告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PsychologicalAssessment>> getAssessmentDetail(
            @PathVariable Long id) {
        PsychologicalAssessment assessment = assessmentService.getAssessmentDetail(id);
        return ResponseEntity.ok(ApiResponse.success(assessment));
    }
}
