package com.tongyangyuan.mentalhealth.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisConnectionChecker implements ApplicationListener<ApplicationReadyEvent> {

    private final StringRedisTemplate redisTemplate;

    public RedisConnectionChecker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            redisTemplate.opsForValue().set("redis_check", "ok");
            String value = redisTemplate.opsForValue().get("redis_check");
            if ("ok".equals(value)) {
                System.out.println("✅ Redis connection successful: " + redisTemplate.getConnectionFactory().getConnection().ping());
                redisTemplate.delete("redis_check");
            } else {
                System.err.println("❌ Redis connection failed: Value mismatch");
            }
        } catch (Exception e) {
            System.err.println("❌ Redis connection failed: " + e.getMessage());
            // Optional: e.printStackTrace();
        }
    }
}
