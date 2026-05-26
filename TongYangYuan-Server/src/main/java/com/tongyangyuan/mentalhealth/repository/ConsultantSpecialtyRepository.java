package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.ConsultantSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultantSpecialtyRepository extends JpaRepository<ConsultantSpecialty, Long> {

    List<ConsultantSpecialty> findByConsultantId(Long consultantId);

    List<ConsultantSpecialty> findByTagId(Long tagId);

    boolean existsByConsultantIdAndTagId(Long consultantId, Long tagId);

    void deleteByConsultantIdAndTagId(Long consultantId, Long tagId);

    void deleteByConsultantId(Long consultantId);

    /**
     * 根据人生阶段编码查找所有关联的咨询师ID
     * tag_id 现在对应 life_stages.id
     */
    @Query(value = "SELECT DISTINCT cs.consultant_id FROM consultant_specialties cs " +
            "JOIN life_stages ls ON cs.tag_id = ls.id " +
            "WHERE ls.code = :stageCode", nativeQuery = true)
    List<Long> findConsultantIdsByStageCode(@Param("stageCode") String stageCode);

    /**
     * 根据人生阶段ID列表查找咨询师ID（匹配任意一个阶段）
     * tag_id 现在对应 life_stages.id
     */
    @Query(value = "SELECT DISTINCT cs.consultant_id FROM consultant_specialties cs " +
            "WHERE cs.tag_id IN :stageIds", nativeQuery = true)
    List<Long> findConsultantIdsByStageIds(@Param("stageIds") List<Long> stageIds);

    /**
     * 根据标签ID列表查找咨询师ID（匹配任意一个标签）- 兼容旧接口
     */
    @Query(value = "SELECT DISTINCT cs.consultant_id FROM consultant_specialties cs " +
            "WHERE cs.tag_id IN :tagIds", nativeQuery = true)
    List<Long> findConsultantIdsByTagIds(@Param("tagIds") List<Long> tagIds);
}
