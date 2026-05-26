package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.ConsultationRecord;
import com.tongyangyuan.mentalhealth.entity.ConsultationSignature;
import com.tongyangyuan.mentalhealth.repository.ConsultationRecordRepository;
import com.tongyangyuan.mentalhealth.repository.ConsultationSignatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/**
 * 咨询签名服务
 */
@Service
public class ConsultationSignatureService {
    
    @Autowired
    private ConsultationSignatureRepository signatureRepository;
    
    @Autowired
    private ConsultationRecordRepository recordRepository;
    
    /**
     * 提交签名
     */
    @Transactional
    public ConsultationSignature submitSignature(Long recordId, String signerType, Long signerId, 
                                                  String signerName, String signatureImage, 
                                                  String verificationMediaUrl, String ipAddress,
                                                  String clientInfo) {
        // 检查是否已有签名
        Optional<ConsultationSignature> existingSig = signatureRepository
                .findByConsultationRecordIdAndSignerType(recordId, signerType);
        
        ConsultationSignature signature;
        if (existingSig.isPresent()) {
            // 更新现有签名
            signature = existingSig.get();
            signature.setSignatureImage(signatureImage);
            signature.setVerificationMediaUrl(verificationMediaUrl);
            signature.setIpAddress(ipAddress);
            signature.setClientInfo(clientInfo);
            signature.setSignedAt(LocalDateTime.now());
        } else {
            // 创建新签名
            signature = new ConsultationSignature();
            signature.setConsultationRecordId(recordId);
            signature.setSignerType(signerType);
            signature.setSignerId(signerId);
            signature.setSignerName(signerName);
            signature.setSignatureImage(signatureImage);
            signature.setVerificationMediaUrl(verificationMediaUrl);
            signature.setIpAddress(ipAddress);
            signature.setClientInfo(clientInfo);
            signature.setSignedAt(LocalDateTime.now());
        }
        
        signature = signatureRepository.save(signature);
        
        // 检查是否双方都已签名
        checkAndUpdateDualSignatureStatus(recordId);
        
        return signature;
    }
    
    /**
     * 获取签名的所有签名
     */
    public List<ConsultationSignature> getSignatures(Long recordId) {
        return signatureRepository.findByConsultationRecordId(recordId);
    }
    
    /**
     * 获取特定类型的签名
     */
    public Optional<ConsultationSignature> getSignatureByType(Long recordId, String signerType) {
        return signatureRepository.findByConsultationRecordIdAndSignerType(recordId, signerType);
    }
    
    /**
     * 检查并更新双方签名状态
     */
    private void checkAndUpdateDualSignatureStatus(Long recordId) {
        List<ConsultationSignature> signatures = signatureRepository.findByConsultationRecordId(recordId);
        
        boolean hasConsultantSig = signatures.stream()
                .anyMatch(s -> "CONSULTANT".equals(s.getSignerType()));
        boolean hasParentSig = signatures.stream()
                .anyMatch(s -> "PARENT".equals(s.getSignerType()));
        
        if (hasConsultantSig && hasParentSig) {
            Optional<ConsultationRecord> recordOpt = recordRepository.findById(recordId);
            if (recordOpt.isPresent()) {
                ConsultationRecord record = recordOpt.get();
                record.setSignatureRecordId(recordId);
                recordRepository.save(record);
            }
        }
    }
    
    /**
     * 获取签名状态
     */
    public Map<String, Boolean> getSignatureStatus(Long recordId) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("consultantSigned", false);
        status.put("parentSigned", false);
        status.put("bothSigned", false);
        
        List<ConsultationSignature> signatures = signatureRepository.findByConsultationRecordId(recordId);
        
        status.put("consultantSigned", signatures.stream()
                .anyMatch(s -> "CONSULTANT".equals(s.getSignerType())));
        status.put("parentSigned", signatures.stream()
                .anyMatch(s -> "PARENT".equals(s.getSignerType())));
        status.put("bothSigned", (Boolean)status.get("consultantSigned") && (Boolean)status.get("parentSigned"));
        
        return status;
    }
    
    /**
     * 删除签名
     */
    @Transactional
    public void deleteSignature(Long signatureId) {
        signatureRepository.deleteById(signatureId);
    }
}