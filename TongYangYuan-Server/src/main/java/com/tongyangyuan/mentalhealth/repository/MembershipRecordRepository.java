package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.MembershipRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会员权益记录 Repository
 */
@Repository
public interface MembershipRecordRepository extends JpaRepository<MembershipRecord, Long> {
    
    List<MembershipRecord> findByUserIdOrderByEndTimeDesc(Long userId);
    
    Optional<MembershipRecord> findByUserIdAndStatus(Long userId, String status);
    
    List<MembershipRecord> findByUserIdAndStatusAndEndTimeAfter(Long userId, String status, LocalDateTime endTime);
    
    Optional<MembershipRecord> findTopByUserIdAndStatusOrderByEndTimeDesc(Long userId, String status);
}
