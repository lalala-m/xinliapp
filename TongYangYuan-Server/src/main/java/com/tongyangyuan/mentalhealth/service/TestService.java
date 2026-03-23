package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.*;
import com.tongyangyuan.mentalhealth.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TestService {
    
    @Autowired
    private TestQuestionRepository questionRepository;
    
    @Autowired
    private UserTestRecordRepository testRecordRepository;
    
    @Autowired
    private AssessmentService assessmentService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取学习包的测试题
     */
    public List<TestQuestion> getTestQuestions(Long packageId) {
        return questionRepository.findByPackageIdAndActiveTrueOrderBySortOrderAsc(packageId);
    }
    
    @Autowired
    private UserLearningService userLearningService;

    /**
     * 提交测试答案
     */
    @Transactional
    public UserTestRecord submitTest(Long userId, Long packageId, String answersJson, Integer timeSpent) {
        List<TestQuestion> questions = getTestQuestions(packageId);
        
        // 解析答案并判分
        Map<String, String> userAnswers = parseAnswers(answersJson);
        int correctCount = 0;
        int totalScore = 0;
        int score = 0;
        
        for (TestQuestion question : questions) {
            totalScore += question.getScore();
            String userAnswer = userAnswers.get(question.getId().toString());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                correctCount++;
                score += question.getScore();
            }
        }
        
        // 创建测试记录
        UserTestRecord record = new UserTestRecord();
        record.setUserId(userId);
        record.setPackageId(packageId);
        record.setTotalQuestions(questions.size());
        record.setCorrectCount(correctCount);
        record.setScore(score);
        record.setTotalScore(totalScore);
        record.setAccuracy(new BigDecimal(correctCount * 100.0 / questions.size()));
        record.setTimeSpent(timeSpent);
        record.setAnswers(answersJson);
        record.setStatus(UserTestRecord.TestStatus.COMPLETED);
        
        UserTestRecord saved = testRecordRepository.save(record);
        
        // 生成评估报告
        assessmentService.generateAssessment(saved);
        
        // 检查是否通过考试（准确率 >= 80%），如果是，尝试升级咨询师
        // if (record.getAccuracy().doubleValue() >= 80.0) {
        //     userLearningService.handleTestPassed(userId, packageId);
        // }
        
        return saved;
    }
    
    /**
     * 获取测试记录
     */
    public Page<UserTestRecord> getUserTestRecords(Long userId, Pageable pageable) {
        return testRecordRepository.findByUserIdOrderByGmtCreateDesc(userId, pageable);
    }
    
    /**
     * 获取测试记录详情
     */
    public UserTestRecord getTestRecordDetail(Long id) {
        return testRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("测试记录不存在"));
    }
    
    private Map<String, String> parseAnswers(String answersJson) {
        try {
            return objectMapper.readValue(answersJson, 
                new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("答案解析失败", e);
        }
    }
}
