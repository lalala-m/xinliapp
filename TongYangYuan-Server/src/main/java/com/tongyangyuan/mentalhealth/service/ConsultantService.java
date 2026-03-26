package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConsultantService {

    private static final Logger log = LoggerFactory.getLogger(ConsultantService.class);

    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;

    public ConsultantService(ConsultantRepository consultantRepository, UserRepository userRepository) {
        this.consultantRepository = consultantRepository;
        this.userRepository = userRepository;
    }

    /**
     * 自动从关联的 User 中取头像填充到 Consultant
     */
    private void fillAvatarFromUser(Consultant consultant) {
        if (consultant == null || consultant.getUserId() == null) return;
        try {
            userRepository.findById(consultant.getUserId()).ifPresent(user -> {
                if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    consultant.setAvatarUrl(user.getAvatarUrl());
                }
            });
        } catch (Exception e) {
            // 忽略用户查找失败，不影响主流程
        }
    }

    public List<Consultant> getAllConsultants() {
        log.info("查询所有咨询师列表 (Cache Miss if seen)");
        List<Consultant> consultants = consultantRepository.findAll();
        // 自动从关联 User 填充头像
        consultants.forEach(this::fillAvatarFromUser);
        // 排序逻辑
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
        Consultant consultant = consultantRepository.findById(id).orElse(null);
        if (consultant != null) fillAvatarFromUser(consultant);
        return consultant;
    }

    public Consultant getConsultantByUserId(Long userId) {
        Consultant consultant = consultantRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("咨询师信息不存在"));
        fillAvatarFromUser(consultant);
        return consultant;
    }

    @Transactional
    public Consultant createOrUpdateConsultant(Consultant consultant) {
        log.info("更新咨询师信息，清除缓存: name={}", consultant.getName());
        return consultantRepository.save(consultant);
    }

    /**
     * 按优先级获取所有可用咨询师
     */
    public List<Consultant> findAllOrderByPriority() {
        List<Consultant> consultants = consultantRepository.findAllOrderByPriority();
        consultants.forEach(this::fillAvatarFromUser);
        return consultants;
    }

    /**
     * 按领域筛选并按优先级排序
     */
    public List<Consultant> findByDomainOrderByPriority(String domain) {
        List<Consultant> consultants = consultantRepository.findByDomainOrderByPriority(domain);
        consultants.forEach(this::fillAvatarFromUser);
        return consultants;
    }

    /**
     * 按领域筛选咨询师
     */
    public List<Consultant> getConsultantsByDomain(String domain) {
        List<Consultant> consultants = consultantRepository.findAll();
        consultants = consultants.stream()
                .filter(c -> c.getSpecialty() != null && c.getSpecialty().contains(domain))
                .collect(java.util.stream.Collectors.toList());
        consultants.forEach(this::fillAvatarFromUser);
        return consultants;
    }
}
