package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConsultantCertificationService {

    @Autowired
    private ConsultantRepository consultantRepository;

    /**
     * 申请黄V认证 (提交证书和擅长领域)
     */
    @Transactional
    public Consultant applyForYellowV(Long userId, String certificateUrl, String specialty) {
        Consultant consultant = consultantRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 如果不存在，创建一个新的待认证咨询师记录
                    Consultant newConsultant = new Consultant();
                    newConsultant.setUserId(userId);
                    newConsultant.setName("待认证用户"); // 暂时默认名，后续可同步用户信息
                    newConsultant.setIdentityTier(Consultant.IdentityTier.BRONZE);
                    return newConsultant;
                });

        consultant.setCertificateUrl(certificateUrl);
        consultant.setSpecialty(specialty);
        consultant.setIsCertificateVerified(false); // 重置审核状态
        // 注意：不重置保证金状态，如果已交过则保留

        return consultantRepository.save(consultant);
    }

    /**
     * 缴纳保证金
     */
    @Transactional
    public Consultant payDeposit(Long userId, BigDecimal amount) {
        Consultant consultant = consultantRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("请先提交认证申请"));

        // 模拟支付逻辑
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("保证金金额必须大于0");
        }

        consultant.setDepositAmount(amount);
        consultant.setIsDepositPaid(true);

        return consultantRepository.save(consultant);
    }

    /**
     * 获取认证状态
     */
    public Map<String, Object> getCertificationStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        consultantRepository.findByUserId(userId).ifPresentOrElse(consultant -> {
            status.put("isApplied", consultant.getCertificateUrl() != null);
            status.put("isDepositPaid", Boolean.TRUE.equals(consultant.getIsDepositPaid()));
            status.put("isVerified", Boolean.TRUE.equals(consultant.getIsCertificateVerified()));
            status.put("identityTier", consultant.getIdentityTier());
            status.put("certificateUrl", consultant.getCertificateUrl());
            status.put("depositAmount", consultant.getDepositAmount());
            status.put("specialty", consultant.getSpecialty());
            
            // 综合状态判断
            String currentStatus = "未申请";
            if (consultant.getIdentityTier() == Consultant.IdentityTier.SILVER) {
                currentStatus = "已认证(黄V)";
            } else if (Boolean.TRUE.equals(consultant.getIsCertificateVerified()) && Boolean.TRUE.equals(consultant.getIsDepositPaid())) {
                 // 理论上应该已经是SILVER，除非手动修改
                 currentStatus = "待生效";
            } else if (consultant.getCertificateUrl() != null) {
                if (!Boolean.TRUE.equals(consultant.getIsDepositPaid())) {
                    currentStatus = "待缴纳保证金";
                } else if (!Boolean.TRUE.equals(consultant.getIsCertificateVerified())) {
                    currentStatus = "待审核";
                }
            }
            status.put("statusText", currentStatus);
            
        }, () -> {
            status.put("isApplied", false);
            status.put("statusText", "未申请");
        });
        return status;
    }

    /**
     * 管理员审核通过
     */
    @Transactional
    public Consultant approveCertification(Long consultantId) {
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new RuntimeException("咨询师不存在"));

        if (!Boolean.TRUE.equals(consultant.getIsDepositPaid())) {
            throw new RuntimeException("该用户尚未缴纳保证金，无法通过认证");
        }

        if (consultant.getCertificateUrl() == null || consultant.getCertificateUrl().isEmpty()) {
             throw new RuntimeException("该用户尚未提交证书");
        }

        consultant.setIsCertificateVerified(true);
        // 只有BRONZE才能升级为SILVER，避免降级
        if (consultant.getIdentityTier() == Consultant.IdentityTier.BRONZE) {
            consultant.setIdentityTier(Consultant.IdentityTier.SILVER);
        }

        return consultantRepository.save(consultant);
    }

    /**
     * 管理员驳回
     */
    @Transactional
    public Consultant rejectCertification(Long consultantId, String reason) {
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new RuntimeException("咨询师不存在"));

        consultant.setIsCertificateVerified(false);
        // 可以在这里添加驳回理由字段记录，或者通过消息通知用户
        // 目前简化为仅重置状态
        
        return consultantRepository.save(consultant);
    }

    /**
     * 获取待审核列表
     */
    public java.util.List<Consultant> getPendingCertifications() {
        // 查找 已上传证书 且 尚未审核通过 的咨询师
        // 实际查询可能需要自定义Query，这里暂时用Stream过滤演示，生产环境应使用JPA Query
        return consultantRepository.findAll().stream()
                .filter(c -> c.getCertificateUrl() != null && !c.getCertificateUrl().isEmpty())
                .filter(c -> !Boolean.TRUE.equals(c.getIsCertificateVerified()))
                .collect(java.util.stream.Collectors.toList());
    }
}
