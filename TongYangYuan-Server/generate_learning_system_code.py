#!/usr/bin/env python3
"""
学习系统代码生成器
自动生成Repository、Service、Controller等代码文件
"""

import os

# 基础路径
BASE_PATH = "TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth"

# Repository模板
REPOSITORY_TEMPLATES = {
    "LearningVideoRepository": """package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.LearningVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningVideoRepository extends JpaRepository<LearningVideo, Long> {
    List<LearningVideo> findByPackageIdAndIsActiveTrueOrderBySortOrderAsc(Long packageId);
    List<LearningVideo> findByPackageId(Long packageId);
    long countByPackageIdAndIsActiveTrue(Long packageId);
}
""",
    
    "UserLearningProgressRepository": """package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserLearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningProgressRepository extends JpaRepository<UserLearningProgress, Long> {
    Optional<UserLearningProgress> findByUserIdAndVideoId(Long userId, Long videoId);
    List<UserLearningProgress> findByUserIdAndPackageId(Long userId, Long packageId);
    List<UserLearningProgress> findByUserId(Long userId);
    
    @Query("SELECT COUNT(ulp) FROM UserLearningProgress ulp WHERE ulp.userId = :userId AND ulp.packageId = :packageId AND ulp.isCompleted = true")
    long countCompletedVideos(@Param("userId") Long userId, @Param("packageId") Long packageId);
}
""",
    
    "TestQuestionRepository": """package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    List<TestQuestion> findByPackageIdAndIsActiveTrueOrderBySortOrderAsc(Long packageId);
    List<TestQuestion> findByPackageId(Long packageId);
    long countByPackageIdAndIsActiveTrue(Long packageId);
}
""",
    
    "UserTestRecordRepository": """package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserTestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserTestRecordRepository extends JpaRepository<UserTestRecord, Long> {
    Page<UserTestRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<UserTestRecord> findByUserIdAndPackageIdOrderByCreatedAtDesc(Long userId, Long packageId);
    List<UserTestRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
""",
    
    "PsychologicalAssessmentRepository": """package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.PsychologicalAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalAssessmentRepository extends JpaRepository<PsychologicalAssessment, Long> {
    Page<PsychologicalAssessment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<PsychologicalAssessment> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<PsychologicalAssessment> findByTestRecordId(Long testRecordId);
}
""",
    
    "UserRecommendedPackageRepository": """package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserRecommendedPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRecommendedPackageRepository extends JpaRepository<UserRecommendedPackage, Long> {
    List<UserRecommendedPackage> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserRecommendedPackage> findByUserIdAndIsViewedFalseOrderByCreatedAtDesc(Long userId);
    Optional<UserRecommendedPackage> findByUserIdAndPackageIdAndConsultationRecordId(Long userId, Long packageId, Long consultationRecordId);
}
"""
}

def generate_repositories():
    """生成Repository文件"""
    repo_path = os.path.join(BASE_PATH, "repository")
    os.makedirs(repo_path, exist_ok=True)
    
    for filename, content in REPOSITORY_TEMPLATES.items():
        filepath = os.path.join(repo_path, f"{filename}.java")
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✓ 已生成: {filename}.java")

def main():
    print("=" * 60)
    print("学习系统代码生成器")
    print("=" * 60)
    
    print("\n正在生成Repository文件...")
    generate_repositories()
    
    print("\n✓ 所有文件生成完成！")
    print("\n提示：")
    print("1. 请检查生成的文件是否正确")
    print("2. 根据需要调整代码")
    print("3. 继续生成Service和Controller层代码")

if __name__ == "__main__":
    main()
