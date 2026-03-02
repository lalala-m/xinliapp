package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserLearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningProgressRepository extends JpaRepository<UserLearningProgress, Long> {
    
    // 查询用户对某个视频的学习进度
    Optional<UserLearningProgress> findByUserIdAndVideoId(Long userId, Long videoId);
    
    // 查询用户在某个学习包的所有学习进度
    List<UserLearningProgress> findByUserIdAndPackageId(Long userId, Long packageId);
    
    // 查询用户的所有学习进度
    List<UserLearningProgress> findByUserId(Long userId);
    
    // 统计用户在某个学习包中已完成的视频数量
    @Query("SELECT COUNT(ulp) FROM UserLearningProgress ulp WHERE ulp.userId = :userId AND ulp.packageId = :packageId AND ulp.isCompleted = true")
    long countCompletedVideos(@Param("userId") Long userId, @Param("packageId") Long packageId);
    
    // 统计用户在某个学习包中通过验证的视频数量
    @Query("SELECT COUNT(ulp) FROM UserLearningProgress ulp WHERE ulp.userId = :userId AND ulp.packageId = :packageId AND ulp.verificationPassed = true")
    long countVerifiedVideos(@Param("userId") Long userId, @Param("packageId") Long packageId);
}
