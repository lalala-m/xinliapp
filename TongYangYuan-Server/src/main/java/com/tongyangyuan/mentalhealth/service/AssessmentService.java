package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.*;
import com.tongyangyuan.mentalhealth.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AssessmentService {
    
    @Autowired
    private PsychologicalAssessmentRepository assessmentRepository;
    
    @Autowired
    private UserTestRecordRepository testRecordRepository;
    
    @Autowired
    private TestQuestionRepository questionRepository;
    
    @Autowired
    private ConsultationRecordRepository consultationRecordRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成评估报告
     */
    @Transactional
    public PsychologicalAssessment generateAssessment(UserTestRecord testRecord) {
        PsychologicalAssessment assessment = new PsychologicalAssessment();
        assessment.setUserId(testRecord.getUserId());
        assessment.setPackageId(testRecord.getPackageId());
        assessment.setTestRecordId(testRecord.getId());
        
        // 分析知识掌握情况
        String knowledgeMastery = analyzeKnowledgeMastery(testRecord);
        assessment.setKnowledgeMastery(knowledgeMastery);
        
        // 评估心理状态
        String psychologicalState = assessPsychologicalState(testRecord);
        assessment.setPsychologicalState(psychologicalState);
        
        // 生成改善建议
        String recommendations = generateRecommendations(testRecord);
        assessment.setRecommendations(recommendations);
        
        // 识别薄弱环节
        String weakPoints = identifyWeakPoints(testRecord);
        assessment.setWeakPoints(weakPoints);
        
        // 生成完整报告数据
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("accuracy", testRecord.getAccuracy());
        reportData.put("score", testRecord.getScore());
        reportData.put("totalScore", testRecord.getTotalScore());
        reportData.put("knowledgeMastery", knowledgeMastery);
        reportData.put("psychologicalState", psychologicalState);
        reportData.put("recommendations", recommendations);
        reportData.put("weakPoints", weakPoints);
        
        try {
            assessment.setReportData(objectMapper.writeValueAsString(reportData));
        } catch (Exception e) {
            throw new RuntimeException("报告数据生成失败", e);
        }
        
        return assessmentRepository.save(assessment);
    }
    
    /**
     * 获取评估报告列表
     */
    public Page<PsychologicalAssessment> getUserAssessments(Long userId, Pageable pageable) {
        return assessmentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取评估报告详情
     */
    public PsychologicalAssessment getAssessmentDetail(Long id) {
        return assessmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("评估报告不存在"));
    }
    
    /**
     * 根据测试记录ID获取评估报告
     */
    public PsychologicalAssessment getAssessmentByTestRecordId(Long testRecordId) {
        return assessmentRepository.findByTestRecordId(testRecordId)
            .orElseThrow(() -> new RuntimeException("评估报告不存在"));
    }
    
    private String analyzeKnowledgeMastery(UserTestRecord testRecord) {
        double accuracy = testRecord.getAccuracy().doubleValue();
        if (accuracy >= 90) {
            return "优秀：您对相关知识点掌握非常扎实，能够灵活运用所学知识。";
        } else if (accuracy >= 75) {
            return "良好：您对大部分知识点掌握较好，建议继续巩固薄弱环节。";
        } else if (accuracy >= 60) {
            return "及格：您对基础知识点有一定了解，建议加强学习和实践。";
        } else {
            return "需加强：建议重新学习相关内容，必要时寻求专业指导。";
        }
    }
    
    private String assessPsychologicalState(UserTestRecord testRecord) {
        double accuracy = testRecord.getAccuracy().doubleValue();
        
        StringBuilder state = new StringBuilder();
        state.append("根据您的答题情况分析：\n");
        
        if (accuracy >= 80) {
            state.append("您的心理状态整体良好，对相关心理知识有较好的理解和认知。");
            state.append("建议继续保持积极的心态，将所学知识应用到日常生活中。");
        } else if (accuracy >= 60) {
            state.append("您对心理健康知识有一定了解，但仍有提升空间。");
            state.append("建议多关注自己的心理状态，必要时寻求专业帮助。");
        } else {
            state.append("建议您更多地关注心理健康知识的学习。");
            state.append("如有需要，可以预约专业咨询师进行深入交流。");
        }
        
        return state.toString();
    }
    
    private String generateRecommendations(UserTestRecord testRecord) {
        double accuracy = testRecord.getAccuracy().doubleValue();
        
        List<String> recommendations = new ArrayList<>();
        
        if (accuracy >= 80) {
            recommendations.add("继续保持良好的学习状态");
            recommendations.add("可以尝试更深入的心理学习内容");
            recommendations.add("将所学知识应用到实际生活中");
        } else if (accuracy >= 60) {
            recommendations.add("建议复习薄弱环节，巩固基础知识");
            recommendations.add("多进行实践练习，加深理解");
            recommendations.add("可以参加相关的学习小组或活动");
        } else {
            recommendations.add("建议重新学习课程内容");
            recommendations.add("可以预约咨询师进行一对一指导");
            recommendations.add("制定系统的学习计划");
        }
        
        return String.join("\n", recommendations);
    }
    
    private String identifyWeakPoints(UserTestRecord testRecord) {
        // 这里可以根据具体的错题情况进行分析
        // 简化实现，返回通用建议
        double accuracy = testRecord.getAccuracy().doubleValue();
        
        if (accuracy < 60) {
            return "需要加强对基础概念的理解和掌握";
        } else if (accuracy < 80) {
            return "需要加强对实际应用场景的理解";
        } else {
            return "整体掌握良好，可以尝试更高难度的内容";
        }
    }
}
