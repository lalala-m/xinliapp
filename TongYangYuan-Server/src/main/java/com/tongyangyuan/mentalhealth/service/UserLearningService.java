package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.*;
import com.tongyangyuan.mentalhealth.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserLearningService {
    
    @Autowired
    private UserLearningProgressRepository progressRepository;
    
    @Autowired
    private LearningVideoRepository videoRepository;
    
    @Autowired
    private LearningPackageRepository packageRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 更新视频观看进度
     */
    @Transactional
    public UserLearningProgress updateVideoProgress(Long userId, Long videoId, Long packageId, 
                                                     Integer watchDuration, Integer lastPosition) {
        Optional<UserLearningProgress> existingOpt = progressRepository
            .findByUserIdAndVideoId(userId, videoId);
        
        UserLearningProgress progress;
        if (existingOpt.isPresent()) {
            progress = existingOpt.get();
        } else {
            progress = new UserLearningProgress();
            progress.setUserId(userId);
            progress.setVideoId(videoId);
            progress.setPackageId(packageId);
        }
        
        progress.setWatchDuration(watchDuration);
        progress.setLastWatchPosition(lastPosition);
        
        // 检查是否完成（观看时长达到视频总时长的90%）
        LearningVideo video = videoRepository.findById(videoId)
            .orElseThrow(() -> new RuntimeException("视频不存在"));
        
        if (watchDuration >= video.getDuration() * 0.9) {
            progress.setIsCompleted(true);
        }
        
        return progressRepository.save(progress);
    }
    
    /**
     * 提交验证题答案
     */
    @Transactional
    public Map<String, Object> submitVerificationAnswer(Long userId, Long videoId, String answer) {
        UserLearningProgress progress = progressRepository
            .findByUserIdAndVideoId(userId, videoId)
            .orElseThrow(() -> new RuntimeException("学习进度不存在"));
        
        LearningVideo video = videoRepository.findById(videoId)
            .orElseThrow(() -> new RuntimeException("视频不存在"));
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析验证题
            @SuppressWarnings("unchecked")
            Map<String, Object> question = objectMapper.readValue(
                video.getVerificationQuestion(), Map.class);
            
            String correctAnswer = (String) question.get("answer");
            boolean isCorrect = correctAnswer.equalsIgnoreCase(answer);
            
            if (isCorrect) {
                progress.setVerificationPassed(true);
                progressRepository.save(progress);
            }
            
            result.put("correct", isCorrect);
            result.put("correctAnswer", correctAnswer);
            result.put("explanation", question.get("explanation"));
            
        } catch (Exception e) {
            throw new RuntimeException("验证题解析失败", e);
        }
        
        return result;
    }
    
    /**
     * 获取用户的学习记录
     */
    public List<Map<String, Object>> getUserLearningRecords(Long userId) {
        List<UserLearningProgress> progressList = progressRepository.findByUserId(userId);
        List<Map<String, Object>> records = new ArrayList<>();
        
        // 按学习包分组
        Map<Long, List<UserLearningProgress>> groupedByPackage = new HashMap<>();
        for (UserLearningProgress progress : progressList) {
            groupedByPackage.computeIfAbsent(progress.getPackageId(), k -> new ArrayList<>())
                .add(progress);
        }
        
        // 构建返回数据
        for (Map.Entry<Long, List<UserLearningProgress>> entry : groupedByPackage.entrySet()) {
            Long packageId = entry.getKey();
            List<UserLearningProgress> progresses = entry.getValue();
            
            Optional<LearningPackage> pkgOpt = packageRepository.findById(packageId);
            if (pkgOpt.isPresent()) {
                LearningPackage pkg = pkgOpt.get();
                
                long completedCount = progresses.stream()
                    .filter(UserLearningProgress::getIsCompleted)
                    .count();
                
                long totalVideos = videoRepository.countByPackageIdAndActiveTrue(packageId);
                
                int totalWatchDuration = progresses.stream()
                    .mapToInt(UserLearningProgress::getWatchDuration)
                    .sum();
                
                Map<String, Object> record = new HashMap<>();
                record.put("package", pkg);
                record.put("completedVideos", completedCount);
                record.put("totalVideos", totalVideos);
                record.put("totalWatchDuration", totalWatchDuration);
                record.put("progress", progresses);
                
                records.add(record);
            }
        }
        
        return records;
    }
    
    @Autowired
    private ConsultantRepository consultantRepository;

    /**
     * 检查用户是否可以参加测试
     */
    public boolean canTakeTest(Long userId, Long packageId) {
        List<LearningVideo> videos = videoRepository
            .findByPackageIdAndActiveTrueOrderBySortOrderAsc(packageId);
        
        long completedCount = progressRepository.countCompletedVideos(userId, packageId);
        long verifiedCount = progressRepository.countVerifiedVideos(userId, packageId);
        
        return completedCount == videos.size() && verifiedCount == videos.size();
    }

    /**
     * 处理考试通过后的逻辑：升级咨询师身份
     */
    @Transactional
    public void handleTestPassed(Long userId, Long packageId) {
        // 1. 查找是否是"加入咨询师团队"的学习包
        Optional<LearningPackage> pkgOpt = packageRepository.findById(packageId);
        if (pkgOpt.isPresent()) {
            LearningPackage pkg = pkgOpt.get();
            if ("加入咨询师团队".equals(pkg.getTitle()) || "Consultant Training".equalsIgnoreCase(pkg.getTitle())) {
                // 2. 查找并升级咨询师
                consultantRepository.findByUserId(userId).ifPresent(consultant -> {
                    // 只有普通用户(BRONZE)才能通过考试升级为蓝V(GOLD)
                    // 如果已经是更高等级(PLATINUM)或已经是GOLD，则不降级/不重复
                    if (consultant.getIdentityTier() == Consultant.IdentityTier.BRONZE) {
                        consultant.setIdentityTier(Consultant.IdentityTier.GOLD);
                        consultantRepository.save(consultant);
                        System.out.println("User " + userId + " promoted to GOLD Consultant via Exam.");
                    }
                });
            }
        }
    }
}
