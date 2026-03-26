package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {
    /**
     * 通过 userId 查找对应的咨询师信息。
     */
    Optional<Consultant> findByUserId(Long userId);

    // 检查是否存在指定用户的咨询师记录
    boolean existsByUserId(Long userId);

    /**
     * 查询所有可用咨询师，按优先级排序
     * 优先级: PLATINUM(1) > GOLD(2) > SILVER(3) > BRONZE(4)
     * 同等级按评分和 服务次数降序
     */
    @Query(value = "SELECT * FROM consultants WHERE is_deleted = 0 AND is_available = true " +
            "ORDER BY " +
            "CASE identity_tier " +
            "  WHEN 'PLATINUM' THEN 1 " +
            "  WHEN 'GOLD' THEN 2 " +
            "  WHEN 'SILVER' THEN 3 " +
            "  WHEN 'BRONZE' THEN 4 " +
            "END, " +
            "rating DESC, " +
            "served_count DESC", nativeQuery = true)
    List<Consultant> findAllOrderByPriority();

    /**
     * 查询特定领域的咨询师，按优先级排序
     */
    @Query(value = "SELECT * FROM consultants WHERE is_deleted = 0 AND is_available = true " +
            "AND specialty LIKE %:domain% " +
            "ORDER BY " +
            "CASE identity_tier " +
            "  WHEN 'PLATINUM' THEN 1 " +
            "  WHEN 'GOLD' THEN 2 " +
            "  WHEN 'SILVER' THEN 3 " +
            "  WHEN 'BRONZE' THEN 4 " +
            "END, " +
            "rating DESC, " +
            "served_count DESC", nativeQuery = true)
    List<Consultant> findByDomainOrderByPriority(String domain);
}
