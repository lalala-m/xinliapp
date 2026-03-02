package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    List<AdminLog> findByAdminUserIdOrderByCreatedAtDesc(Long adminUserId);
    List<AdminLog> findTop10ByOrderByCreatedAtDesc();
}
