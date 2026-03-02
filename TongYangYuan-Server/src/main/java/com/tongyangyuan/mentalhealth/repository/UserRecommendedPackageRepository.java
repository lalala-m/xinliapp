package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserRecommendedPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRecommendedPackageRepository extends JpaRepository<UserRecommendedPackage, Long> {
    
    // 查询用户的所有推荐学习包
    List<UserRecommendedPackage> findByUserIdOrderByGmtCreateDesc(Long userId);
    
    // 查询用户未查看的推荐学习包
    List<UserRecommendedPackage> findByUserIdAndViewedFalseOrderByGmtCreateDesc(Long userId);
    
    // 查询特定的推荐记录
    Optional<UserRecommendedPackage> findByUserIdAndPackageIdAndConsultationRecordId(
        Long userId, Long packageId, Long consultationRecordId);
}
