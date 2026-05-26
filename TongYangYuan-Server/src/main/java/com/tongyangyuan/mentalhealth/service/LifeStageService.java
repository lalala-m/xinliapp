package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.ConsultationIssue;
import com.tongyangyuan.mentalhealth.entity.LifeStage;
import com.tongyangyuan.mentalhealth.repository.ConsultationIssueRepository;
import com.tongyangyuan.mentalhealth.repository.LifeStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LifeStageService {

    private final LifeStageRepository lifeStageRepository;
    private final ConsultationIssueRepository issueRepository;

    public LifeStageService(LifeStageRepository lifeStageRepository,
                            ConsultationIssueRepository issueRepository) {
        this.lifeStageRepository = lifeStageRepository;
        this.issueRepository = issueRepository;
    }

    // ========== 公开查询接口 ==========

    /**
     * 获取所有启用的阶段（含问题）
     */
    public List<LifeStage> getAllEnabledStagesWithIssues() {
        return lifeStageRepository.findAllEnabledWithIssues();
    }

    /**
     * 获取所有启用的阶段（不含问题，轻量）
     */
    public List<LifeStage> getAllEnabledStages() {
        return lifeStageRepository.findByIsEnabledTrueOrderBySortOrderAsc();
    }

    /**
     * 根据code获取阶段详情（含问题）
     */
    public LifeStage getStageByCodeWithIssues(String code) {
        return lifeStageRepository.findByCodeWithIssues(code)
                .orElseThrow(() -> new RuntimeException("阶段不存在: " + code));
    }

    /**
     * 获取某阶段下的所有启用问题，按分类分组
     */
    public Map<String, List<ConsultationIssue>> getIssuesGroupedByCategory(Long stageId) {
        List<ConsultationIssue> issues = issueRepository.findByStageIdAndIsEnabledTrueOrderBySortOrderAsc(stageId);
        return issues.stream().collect(Collectors.groupingBy(ConsultationIssue::getCategoryName));
    }

    // ========== Admin 管理接口 ==========

    /**
     * 获取所有阶段（含禁用，用于管理）
     */
    @Transactional(readOnly = true)
    public List<LifeStage> getAllStages() {
        return lifeStageRepository.findAllWithIssues();
    }

    /**
     * 创建阶段
     */
    @Transactional
    public LifeStage createStage(LifeStage stage) {
        if (lifeStageRepository.existsByCode(stage.getCode())) {
            throw new RuntimeException("阶段编码已存在: " + stage.getCode());
        }
        return lifeStageRepository.save(stage);
    }

    /**
     * 更新阶段
     */
    @Transactional
    public LifeStage updateStage(Long id, LifeStage stage) {
        LifeStage existing = lifeStageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("阶段不存在: " + id));
        
        // 如果code变更，检查新code是否已存在
        if (!existing.getCode().equals(stage.getCode()) && lifeStageRepository.existsByCode(stage.getCode())) {
            throw new RuntimeException("阶段编码已存在: " + stage.getCode());
        }
        
        existing.setCode(stage.getCode());
        existing.setName(stage.getName());
        existing.setIcon(stage.getIcon());
        existing.setSortOrder(stage.getSortOrder());
        existing.setIsEnabled(stage.getIsEnabled());
        
        return lifeStageRepository.save(existing);
    }

    /**
     * 删除阶段（级联删除问题）
     */
    @Transactional
    public void deleteStage(Long id) {
        LifeStage stage = lifeStageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("阶段不存在: " + id));
        lifeStageRepository.delete(stage);
    }

    /**
     * 创建问题
     */
    @Transactional
    public ConsultationIssue createIssue(ConsultationIssue issue) {
        if (!lifeStageRepository.existsById(issue.getStageId())) {
            throw new RuntimeException("阶段不存在: " + issue.getStageId());
        }
        return issueRepository.save(issue);
    }

    /**
     * 更新问题
     */
    @Transactional
    public ConsultationIssue updateIssue(Long id, ConsultationIssue issue) {
        ConsultationIssue existing = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在: " + id));
        
        existing.setCategoryName(issue.getCategoryName());
        existing.setIssueName(issue.getIssueName());
        existing.setIsCustomInput(issue.getIsCustomInput());
        existing.setSortOrder(issue.getSortOrder());
        existing.setIsEnabled(issue.getIsEnabled());
        
        return issueRepository.save(existing);
    }

    /**
     * 删除问题
     */
    @Transactional
    public void deleteIssue(Long id) {
        issueRepository.deleteById(id);
    }

    /**
     * 获取某阶段下的所有问题（管理用）
     */
    public List<ConsultationIssue> getIssuesByStageId(Long stageId) {
        return issueRepository.findByStageIdOrderBySortOrderAsc(stageId);
    }
}
