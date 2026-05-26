package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.RechargePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechargePackageRepository extends JpaRepository<RechargePackage, Long> {

    List<RechargePackage> findByIsActiveTrueOrderBySortOrderAsc();
}
