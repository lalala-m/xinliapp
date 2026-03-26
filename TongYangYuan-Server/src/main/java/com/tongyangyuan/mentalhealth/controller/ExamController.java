package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.TestQuestion;
import com.tongyangyuan.mentalhealth.entity.UserBadge;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.TestQuestionRepository;
import com.tongyangyuan.mentalhealth.repository.UserBadgeRepository;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/learning/exam")
public class ExamController {

    private final TestQuestionRepository testQuestionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final ConsultantRepository consultantRepository;
    private final JwtUtil jwtUtil;

    public ExamController(TestQuestionRepository testQuestionRepository,
                         UserBadgeRepository userBadgeRepository,
                         ConsultantRepository consultantRepository,
                         JwtUtil jwtUtil) {
        this.testQuestionRepository = testQuestionRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.consultantRepository = consultantRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取考试题目
     * GET /learning/exam/{categoryId}
     */
    @GetMapping("/{categoryId}")
    public ApiResponse<List<TestQuestion>> getExamQuestions(@PathVariable Long categoryId) {
        try {
            List<TestQuestion> questions = testQuestionRepository.findByPackageIdOrderBySortOrderAsc(categoryId);
            // 随机打乱题目顺序
            Collections.shuffle(questions);
            // 不返回正确答案
            questions.forEach(q -> q.setCorrectAnswer(null));
            return ApiResponse.success(questions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 提交考试
     * POST /learning/exam/{categoryId}/submit
     */
    @PostMapping("/{categoryId}/submit")
    public ApiResponse<Map<String, Object>> submitExam(
            @PathVariable Long categoryId,
            @RequestBody ExamSubmission submission,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            
            // 获取所有题目
            List<TestQuestion> questions = testQuestionRepository.findByPackageIdOrderBySortOrderAsc(categoryId);
            Map<Long, String> questionMap = new HashMap<>();
            questions.forEach(q -> questionMap.put(q.getId(), q.getCorrectAnswer()));
            
            // 评分
            int correctCount = 0;
            int totalCount = submission.getAnswers() != null ? submission.getAnswers().size() : 0;
            
            if (submission.getAnswers() != null) {
                for (Map.Entry<Long, String> entry : submission.getAnswers().entrySet()) {
                    String correct = questionMap.get(entry.getKey());
                    if (correct != null && correct.equalsIgnoreCase(entry.getValue().trim())) {
                        correctCount++;
                    }
                }
            }
            
            int score = totalCount > 0 ? (correctCount * 100 / totalCount) : 0;
            boolean passed = score >= 60; // 60分及格
            
            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("correctCount", correctCount);
            result.put("totalCount", totalCount);
            result.put("passed", passed);
            
            // 如果通过，颁发智慧妈妈徽章
            if (passed) {
                // 检查是否已有徽章
                if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "wisdom_mom")) {
                    UserBadge badge = new UserBadge();
                    badge.setUserId(userId);
                    badge.setBadgeId("wisdom_mom");
                    badge.setBadgeName("智慧妈妈");
                    badge.setBadgeIcon("badge_wisdom_mom.png");
                    badge.setCategory(submission.getCategory());
                    badge.setEarnedAt(LocalDateTime.now());
                    userBadgeRepository.save(badge);
                    
                    // 升级为GOLD咨询师
                    consultantRepository.findByUserId(userId).ifPresent(consultant -> {
                        consultant.setIdentityTier(Consultant.IdentityTier.GOLD);
                        if (consultant.getSpecialty() == null || consultant.getSpecialty().isEmpty()) {
                            consultant.setSpecialty(submission.getCategory());
                        }
                        consultantRepository.save(consultant);
                    });
                    
                    result.put("earnedBadge", badge);
                } else {
                    // 已有徽章，返回已有徽章信息
                    userBadgeRepository.findByUserIdAndBadgeId(userId, "wisdom_mom").ifPresent(badge -> {
                        Map<String, Object> badgeInfo = new HashMap<>();
                        badgeInfo.put("badgeId", badge.getBadgeId());
                        badgeInfo.put("badgeName", badge.getBadgeName());
                        result.put("earnedBadge", badgeInfo);
                    });
                }
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户徽章
     * GET /learning/badges
     */
    @GetMapping("/badges")
    public ApiResponse<List<UserBadge>> getUserBadges(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            List<UserBadge> badges = userBadgeRepository.findByUserId(userId);
            return ApiResponse.success(badges);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查用户是否有徽章
     * GET /learning/badges/{badgeId}
     */
    @GetMapping("/badges/{badgeId}")
    public ApiResponse<Map<String, Object>> checkBadge(
            @PathVariable String badgeId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            boolean hasBadge = userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasBadge", hasBadge);
            
            if (hasBadge) {
                userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId).ifPresent(badge -> {
                    result.put("badgeName", badge.getBadgeName());
                    result.put("earnedAt", badge.getEarnedAt());
                });
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 请求体
    public static class ExamSubmission {
        private Long categoryId;
        private String category;
        private Map<Long, String> answers;

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Map<Long, String> getAnswers() {
            return answers;
        }

        public void setAnswers(Map<Long, String> answers) {
            this.answers = answers;
        }
    }
}
