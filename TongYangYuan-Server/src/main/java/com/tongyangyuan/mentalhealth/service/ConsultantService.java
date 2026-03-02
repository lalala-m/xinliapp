package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConsultantService {

    private static final Logger log = LoggerFactory.getLogger(ConsultantService.class);

    private final ConsultantRepository consultantRepository;

    public ConsultantService(ConsultantRepository consultantRepository) {
        this.consultantRepository = consultantRepository;
    }

    public List<Consultant> getAllConsultants() {
        log.info("查询所有咨询师列表 (Cache Miss if seen)");
        List<Consultant> consultants = consultantRepository.findAll();
        // 排序逻辑: 内部人员 (PLATINUM) > 蓝V (GOLD) > 黄V (SILVER) > 其他
        // 辅助排序: 评分高 > 服务人数多
        consultants.sort((c1, c2) -> {
            int tier1 = getTierPriority(c1.getIdentityTier());
            int tier2 = getTierPriority(c2.getIdentityTier());
            
            if (tier1 != tier2) {
                return tier2 - tier1; // 优先级高的在前 (数值大在前)
            }
            
            // 同级按评分
            int ratingCompare = c2.getRating().compareTo(c1.getRating());
            if (ratingCompare != 0) {
                return ratingCompare;
            }
            
            // 同评分按服务人数
            return c2.getServedCount() - c1.getServedCount();
        });
        return consultants;
    }

    private int getTierPriority(Consultant.IdentityTier tier) {
        if (tier == null) return 0;
        switch (tier) {
            case PLATINUM: return 4; // 内部人员
            case GOLD: return 3;     // 蓝V
            case SILVER: return 2;   // 黄V
            case BRONZE: return 1;
            default: return 0;
        }
    }

    public Consultant getConsultantById(Long id) {
        return consultantRepository.findById(id)
                .orElse(null); // 返回null而不是抛出异常，方便Controller处理
    }

    public Consultant getConsultantByUserId(Long userId) {
        return consultantRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("咨询师信息不存在"));
    }

    @Transactional
    public Consultant createOrUpdateConsultant(Consultant consultant) {
        log.info("更新咨询师信息，清除缓存: name={}", consultant.getName());
        return consultantRepository.save(consultant);
    }
}
