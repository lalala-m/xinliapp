package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {
    /**
     * 通过 userId 查找对应的咨询师信息。
     */
    Optional<Consultant> findByUserId(Long userId);

    // 检查是否存在指定用户的咨询师记录
    boolean existsByUserId(Long userId);
}
