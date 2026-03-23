package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserTestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTestRecordRepository extends JpaRepository<UserTestRecord, Long> {
    
    // 分页查询用户的测试记录
    Page<UserTestRecord> findByUserIdOrderByGmtCreateDesc(Long userId, Pageable pageable);
    
    // 查询用户在某个学习包的测试记录
    List<UserTestRecord> findByUserIdAndPackageIdOrderByGmtCreateDesc(Long userId, Long packageId);
    
    // 查询用户的所有测试记录
    List<UserTestRecord> findByUserIdOrderByGmtCreateDesc(Long userId);
}
