package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.ConsultationSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 咨询签名仓储
 */
@Repository
public interface ConsultationSignatureRepository extends JpaRepository<ConsultationSignature, Long> {
    
    /**
     * 根据咨询记录ID查找所有签名
     */
    List<ConsultationSignature> findByConsultationRecordId(Long consultationRecordId);
    
    /**
     * 根据咨询记录ID和签名者类型查找签名
     */
    Optional<ConsultationSignature> findByConsultationRecordIdAndSignerType(Long consultationRecordId, String signerType);
    
    /**
     * 根据咨询记录ID查找咨询师签名
     */
    Optional<ConsultationSignature> findByConsultationRecordIdAndSignerTypeAndPdfIncorporatedFalse(Long consultationRecordId, String signerType);
}