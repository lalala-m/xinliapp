package com.tongyangyuan.mentalhealth.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            List<Map<String, String>> banners = getBanners();
            config.put("banners", banners);
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

        List<Map<String, String>> defaultBanners = new ArrayList<>();
        Map<String, String> b1 = new HashMap<>();
        b1.put("image", "");
        b1.put("link", "");
        b1.put("title", "💎 会员限时优惠");
        b1.put("subtitle", "年度会员8折优惠");
        defaultBanners.add(b1);

        Map<String, String> b2 = new HashMap<>();
        b2.put("image", "");
        b2.put("link", "");
        b2.put("title", "👨‍⚕️ 专业导师团队");
        b2.put("subtitle", "30+认证咨询师");
        defaultBanners.add(b2);

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

    @RequestMapping(value = "/admin/banners", method = {RequestMethod.POST, RequestMethod.PUT})
    public ApiResponse<String> updateBanners(@RequestBody String body) {
        try {
            if (body == null || body.trim().isEmpty()) {
                return ApiResponse.error("请求体为空");
            }
            
            // 解析JSON数组
            List<Map<String, String>> banners = objectMapper.readValue(body, 
                new TypeReference<List<Map<String, String>>>() {});
            
            String json = objectMapper.writeValueAsString(banners);
            redisTemplate.opsForValue().set(REDIS_KEY_BANNERS, json);
            return ApiResponse.success("更新成功", "已设置 " + banners.size() + " 张轮播图");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/admin/adCard", method = {RequestMethod.POST, RequestMethod.PUT})
    public ApiResponse<String> updateAdCard(@RequestBody String body) {
        try {
            if (body == null || body.trim().isEmpty()) {
                return ApiResponse.error("请求体为空");
            }
            Map<String, String> adCard = objectMapper.readValue(body, 
                new TypeReference<Map<String, String>>() {});
            String json = objectMapper.writeValueAsString(adCard);
            redisTemplate.opsForValue().set(REDIS_KEY_AD_CARD, json);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
