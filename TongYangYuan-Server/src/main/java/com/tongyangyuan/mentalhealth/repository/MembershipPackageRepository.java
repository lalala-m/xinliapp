package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.MembershipPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会员套餐 Repository
 */
@Repository
public interface MembershipPackageRepository extends JpaRepository<MembershipPackage, Long> {
    
    Optional<MembershipPackage> findByCode(String code);
    
    List<MembershipPackage> findByIsActiveTrueOrderBySortOrderAsc();
}
