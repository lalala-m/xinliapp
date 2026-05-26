package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.ConsultantSpecialty;
import com.tongyangyuan.mentalhealth.entity.LifeStage;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.ConsultantSpecialtyRepository;
import com.tongyangyuan.mentalhealth.repository.LifeStageRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConsultantService {

    private static final Logger log = LoggerFactory.getLogger(ConsultantService.class);

    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;
    private final ConsultantSpecialtyRepository consultantSpecialtyRepository;
    private final LifeStageRepository lifeStageRepository;

    public ConsultantService(ConsultantRepository consultantRepository,
                             UserRepository userRepository,
                             ConsultantSpecialtyRepository consultantSpecialtyRepository,
                             LifeStageRepository lifeStageRepository) {
        this.consultantRepository = consultantRepository;
        this.userRepository = userRepository;
        this.consultantSpecialtyRepository = consultantSpecialtyRepository;
        this.lifeStageRepository = lifeStageRepository;
    }

    /**
     * 将数据库tier映射为前端展示的tier值
     * PLATINUM → YELLOW_V, GOLD → BLUE_V, BRONZE/SILVER → INTERNAL
     */
    private void mapIdentityTierForFrontend(Consultant consultant) {
        if (consultant == null || consultant.getIdentityTier() == null) return;
        switch (consultant.getIdentityTier()) {
            case PLATINUM:
                consultant.setIdentityTier(Consultant.IdentityTier.YELLOW_V);
                break;
            case GOLD:
                consultant.setIdentityTier(Consultant.IdentityTier.BLUE_V);
                break;
            case BRONZE:
            case SILVER:
                consultant.setIdentityTier(Consultant.IdentityTier.INTERNAL);
                break;
            default:
                // YELLOW_V, BLUE_V, INTERNAL 保持不变
                break;
        }
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

    /**
     * 填充咨询师的人生阶段标签
     */
    private void fillLifeStages(Consultant consultant) {
        if (consultant == null || consultant.getId() == null) return;
        try {
            List<ConsultantSpecialty> csList = consultantSpecialtyRepository.findByConsultantId(consultant.getId());
            List<Long> stageIds = csList.stream()
                    .map(ConsultantSpecialty::getTagId)
                    .collect(Collectors.toList());
            if (!stageIds.isEmpty()) {
                List<LifeStage> stages = lifeStageRepository.findAllById(stageIds);
                consultant.setStages(stages);
            }
        } catch (Exception e) {
            // 忽略查询失败，不影响主流程
            log.warn("填充咨询师人生阶段失败: consultantId={}", consultant.getId(), e);
        }
    }

    public List<Consultant> getAllConsultants() {
        log.info("查询所有咨询师列表 (Cache Miss if seen)");
        List<Consultant> consultants = consultantRepository.findAll();
        // 自动从关联 User 填充头像，并映射tier为前端值
        consultants.forEach(c -> {
            fillAvatarFromUser(c);
            mapIdentityTierForFrontend(c);
            fillLifeStages(c);
        });
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
        if (consultant != null) {
            fillAvatarFromUser(consultant);
            mapIdentityTierForFrontend(consultant);
            fillLifeStages(consultant);
        }
        return consultant;
    }

    public Consultant getConsultantByUserId(Long userId) {
        Consultant consultant = consultantRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("咨询师信息不存在"));
        fillAvatarFromUser(consultant);
        mapIdentityTierForFrontend(consultant);
        fillLifeStages(consultant);
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
        consultants.forEach(c -> {
            fillAvatarFromUser(c);
            mapIdentityTierForFrontend(c);
            fillLifeStages(c);
        });
        return consultants;
    }

    /**
     * 按领域筛选并按优先级排序
     */
    public List<Consultant> findByDomainOrderByPriority(String domain) {
        List<Consultant> consultants = consultantRepository.findByDomainOrderByPriority(domain);
        consultants.forEach(c -> {
            fillAvatarFromUser(c);
            mapIdentityTierForFrontend(c);
            fillLifeStages(c);
        });
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
        consultants.forEach(c -> {
            fillAvatarFromUser(c);
            mapIdentityTierForFrontend(c);
            fillLifeStages(c);
        });
        return consultants;
    }

    /**
     * 按人生阶段编码筛选咨询师
     */
    public List<Consultant> findByStageCode(String stageCode) {
        List<Long> consultantIds = consultantSpecialtyRepository.findConsultantIdsByStageCode(stageCode);
        if (consultantIds.isEmpty()) {
            return List.of();
        }
        List<Consultant> consultants = consultantRepository.findAllById(consultantIds);
        consultants.forEach(c -> {
            fillAvatarFromUser(c);
            mapIdentityTierForFrontend(c);
            fillLifeStages(c);
        });
        consultants.sort((c1, c2) -> {
            int tier1 = getTierPriority(c1.getIdentityTier());
            int tier2 = getTierPriority(c2.getIdentityTier());
            if (tier1 != tier2) return tier2 - tier1;
            int ratingCompare = c2.getRating().compareTo(c1.getRating());
            if (ratingCompare != 0) return ratingCompare;
            return c2.getServedCount() - c1.getServedCount();
        });
        return consultants;
    }

    /**
     * 按人生阶段ID列表筛选咨询师（匹配任意一个阶段）
     */
    public List<Consultant> findByStageIds(List<Long> stageIds) {
        List<Long> consultantIds = consultantSpecialtyRepository.findConsultantIdsByStageIds(stageIds);
        List<Consultant> consultants;
        if (consultantIds.isEmpty()) {
            // Fallback: 通过阶段名称模糊匹配 specialty 文本字段
            List<LifeStage> stages = lifeStageRepository.findAllById(stageIds);
            if (stages.isEmpty()) {
                return List.of();
            }
            consultants = consultantRepository.findAll();
            consultants = consultants.stream()
                    .filter(c -> {
                        if (c.getSpecialty() == null || c.getSpecialty().isEmpty()) return false;
                        String specialty = c.getSpecialty();
                        return stages.stream().anyMatch(s -> specialty.contains(s.getName()));
                    })
                    .collect(Collectors.toList());
        } else {
            consultants = consultantRepository.findAllById(consultantIds);
        }
        consultants.forEach(c -> {
            fillAvatarFromUser(c);
            mapIdentityTierForFrontend(c);
            fillLifeStages(c);
        });
        consultants.sort((c1, c2) -> {
            int tier1 = getTierPriority(c1.getIdentityTier());
            int tier2 = getTierPriority(c2.getIdentityTier());
            if (tier1 != tier2) return tier2 - tier1;
            int ratingCompare = c2.getRating().compareTo(c1.getRating());
            if (ratingCompare != 0) return ratingCompare;
            return c2.getServedCount() - c1.getServedCount();
        });
        return consultants;
    }

    /**
     * 按人生阶段ID列表筛选咨询师
     */
    public List<Consultant> findByTagIds(List<Long> stageIds) {
        return findByStageIds(stageIds);
    }

    /**
     * 按人生阶段编码筛选咨询师
     */
    public List<Consultant> findByCategoryCode(String stageCode) {
        return findByStageCode(stageCode);
    }

    /**
     * 获取咨询师的所有人生阶段标签
     */
    public List<LifeStage> getConsultantLifeStages(Long consultantId) {
        List<ConsultantSpecialty> csList = consultantSpecialtyRepository.findByConsultantId(consultantId);
        List<Long> stageIds = csList.stream().map(ConsultantSpecialty::getTagId).collect(Collectors.toList());
        if (stageIds.isEmpty()) {
            return List.of();
        }
        return lifeStageRepository.findAllById(stageIds);
    }
}
