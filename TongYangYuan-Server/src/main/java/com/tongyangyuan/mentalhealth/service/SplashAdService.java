package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.SplashAd;
import com.tongyangyuan.mentalhealth.repository.SplashAdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SplashAdService {

    private final SplashAdRepository splashAdRepository;

    public SplashAdService(SplashAdRepository splashAdRepository) {
        this.splashAdRepository = splashAdRepository;
    }

    /**
     * 获取当前有效的开屏广告（客户端调用）
     */
    @Transactional(readOnly = true)
    public List<SplashAd> getActiveAds() {
        return splashAdRepository.findActiveAds(LocalDate.now());
    }

    /**
     * 获取所有广告（管理后台用）
     */
    @Transactional(readOnly = true)
    public List<SplashAd> getAllAds() {
        return splashAdRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 根据ID获取广告
     */
    @Transactional(readOnly = true)
    public SplashAd getAdById(Long id) {
        return splashAdRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("广告不存在: " + id));
    }

    /**
     * 创建广告
     */
    @Transactional
    public SplashAd createAd(SplashAd ad) {
        return splashAdRepository.save(ad);
    }

    /**
     * 更新广告
     */
    @Transactional
    public SplashAd updateAd(Long id, SplashAd ad) {
        SplashAd existing = getAdById(id);
        existing.setTitle(ad.getTitle());
        existing.setImageUrl(ad.getImageUrl());
        existing.setLinkUrl(ad.getLinkUrl());
        existing.setLinkType(ad.getLinkType());
        existing.setDurationSeconds(ad.getDurationSeconds());
        existing.setIsEnabled(ad.getIsEnabled());
        existing.setIsSkippable(ad.getIsSkippable());
        existing.setStartDate(ad.getStartDate());
        existing.setEndDate(ad.getEndDate());
        existing.setSortOrder(ad.getSortOrder());
        return splashAdRepository.save(existing);
    }

    /**
     * 删除广告
     */
    @Transactional
    public void deleteAd(Long id) {
        if (!splashAdRepository.existsById(id)) {
            throw new RuntimeException("广告不存在: " + id);
        }
        splashAdRepository.deleteById(id);
    }

    /**
     * 记录广告展示
     */
    @Transactional
    public void recordShow(Long id) {
        splashAdRepository.incrementShowCount(id);
    }

    /**
     * 记录广告点击
     */
    @Transactional
    public void recordClick(Long id) {
        splashAdRepository.incrementClickCount(id);
    }
}
