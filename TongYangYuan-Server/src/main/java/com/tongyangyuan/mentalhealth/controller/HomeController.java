package com.tongyangyuan.mentalhealth.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 首页配置接口：轮播广告、广告卡片
 * 管理后台可通过 PUT 接口更新，前端通过 GET 获取
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    private static final String REDIS_KEY_BANNERS = "home:banners";
    private static final String REDIS_KEY_AD_CARD = "home:adCard";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public HomeController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取首页配置（轮播图 + 广告卡片）
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();

            // 轮播图
            List<Map<String, String>> banners = getBanners();
            config.put("banners", banners);

            // 广告卡片
            Map<String, String> adCard = getAdCard();
            config.put("adCard", adCard);

            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private List<Map<String, String>> getBanners() {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_KEY_BANNERS);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
            }
        } catch (Exception ignored) {}

        // 默认轮播（共3张）
        List<Map<String, String>> defaultBanners = new ArrayList<>();

        Map<String, String> b1 = new HashMap<>();
        b1.put("image", "");
        b1.put("link", "");
        b1.put("title", "💎 会员限时优惠");
        b1.put("subtitle", "年度会员8折优惠，享受全年无限次咨询");
        defaultBanners.add(b1);

        Map<String, String> b2 = new HashMap<>();
        b2.put("image", "");
        b2.put("link", "");
        b2.put("title", "👨‍⚕️ 专业导师团队");
        b2.put("subtitle", "30+认证咨询师全程陪伴，定制专属方案");
        defaultBanners.add(b2);

        Map<String, String> b3 = new HashMap<>();
        b3.put("image", "");
        b3.put("link", "");
        b3.put("title", "🎁 首单优惠40%");
        b3.put("subtitle", "轻松打开心灵窗户，让爱陪伴成长每一步");
        defaultBanners.add(b3);

        return defaultBanners;
    }

    private Map<String, String> getAdCard() {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_KEY_AD_CARD);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception ignored) {}

        Map<String, String> defaultAd = new HashMap<>();
        defaultAd.put("image", "");
        defaultAd.put("title", "首单优惠 40%");
        defaultAd.put("subtitle", "轻松打开心灵窗户");
        defaultAd.put("link", "");
        defaultAd.put("buttonText", "点击咨询");
        return defaultAd;
    }

    /**
     * 管理后台：更新轮播图配置
     * @param banners [{"image":"url","link":"","title":"","subtitle":""}]
     */
    @PutMapping("/admin/banners")
    public ApiResponse<Void> updateBanners(@RequestBody List<Map<String, String>> banners) {
        try {
            String json = objectMapper.writeValueAsString(banners != null ? banners : Collections.emptyList());
            redisTemplate.opsForValue().set(REDIS_KEY_BANNERS, json);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 管理后台：更新广告卡片配置
     */
    @PutMapping("/admin/adCard")
    public ApiResponse<Void> updateAdCard(@RequestBody Map<String, String> adCard) {
        try {
            String json = objectMapper.writeValueAsString(adCard != null ? adCard : Collections.emptyMap());
            redisTemplate.opsForValue().set(REDIS_KEY_AD_CARD, json);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
