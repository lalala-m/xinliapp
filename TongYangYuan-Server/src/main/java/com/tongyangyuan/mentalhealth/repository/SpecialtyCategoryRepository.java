package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.SpecialtyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyCategoryRepository extends JpaRepository<SpecialtyCategory, Long> {

    Optional<SpecialtyCategory> findByCode(String code);

    @Query("SELECT sc FROM SpecialtyCategory sc LEFT JOIN FETCH sc.tags ORDER BY sc.sortOrder ASC")
    List<SpecialtyCategory> findAllWithTags();

    List<SpecialtyCategory> findAllByOrderBySortOrderAsc();
}
