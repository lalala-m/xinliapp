package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.SpecialtyTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialtyTagRepository extends JpaRepository<SpecialtyTag, Long> {

    List<SpecialtyTag> findByCategoryIdOrderBySortOrderAsc(Long categoryId);

    List<SpecialtyTag> findByCategoryId(Long categoryId);
}
