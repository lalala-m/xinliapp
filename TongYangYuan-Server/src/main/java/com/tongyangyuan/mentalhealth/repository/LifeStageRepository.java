package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.LifeStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LifeStageRepository extends JpaRepository<LifeStage, Long> {

    Optional<LifeStage> findByCode(String code);

    List<LifeStage> findByIsEnabledTrueOrderBySortOrderAsc();

    boolean existsByCode(String code);

    @Query("SELECT ls FROM LifeStage ls LEFT JOIN FETCH ls.issues i WHERE ls.isEnabled = true AND (i.isEnabled = true OR i IS NULL) ORDER BY ls.sortOrder ASC")
    List<LifeStage> findAllEnabledWithIssues();

    @Query("SELECT ls FROM LifeStage ls LEFT JOIN FETCH ls.issues i WHERE ls.code = :code AND ls.isEnabled = true AND (i.isEnabled = true OR i IS NULL)")
    Optional<LifeStage> findByCodeWithIssues(@Param("code") String code);

    @Query("SELECT ls FROM LifeStage ls LEFT JOIN FETCH ls.issues i ORDER BY ls.sortOrder ASC")
    List<LifeStage> findAllWithIssues();
}
