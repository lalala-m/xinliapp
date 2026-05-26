package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.SplashAd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SplashAdRepository extends JpaRepository<SplashAd, Long> {

    /**
     * 查询所有启用的、在有效期内的广告，按排序号排序
     */
    @Query("SELECT sa FROM SplashAd sa WHERE sa.isEnabled = true " +
           "AND (sa.startDate IS NULL OR sa.startDate <= :today) " +
           "AND (sa.endDate IS NULL OR sa.endDate >= :today) " +
           "ORDER BY sa.sortOrder ASC")
    List<SplashAd> findActiveAds(@Param("today") LocalDate today);

    /**
     * 查询所有广告（管理后台用）
     */
    List<SplashAd> findAllByOrderBySortOrderAsc();

    /**
     * 增加展示次数
     */
    @Modifying
    @Query("UPDATE SplashAd sa SET sa.showCount = sa.showCount + 1 WHERE sa.id = :id")
    void incrementShowCount(@Param("id") Long id);

    /**
     * 增加点击次数
     */
    @Modifying
    @Query("UPDATE SplashAd sa SET sa.clickCount = sa.clickCount + 1 WHERE sa.id = :id")
    void incrementClickCount(@Param("id") Long id);
}
