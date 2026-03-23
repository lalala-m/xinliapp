package com.jslh.tenant.redis;

import com.jslh.commons.tools.redis.RedisKeys;
import com.jslh.commons.tools.redis.RedisUtils;
import com.jslh.tenant.dto.SysTenantDataSourceDTO;
import com.jslh.tenant.dto.SysTenantListDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 租户缓存
 */
@Component
public class SysTenantRedis {
    @Resource
    private RedisUtils redisUtils;

    public void setCache(List<SysTenantListDTO> list) {
        String key = RedisKeys.getTenantKey();
        redisUtils.set(key, list, RedisUtils.NOT_EXPIRE);
    }

    public List<SysTenantListDTO> getCache() {
        String key = RedisKeys.getTenantKey();
        return (List<SysTenantListDTO>) redisUtils.get(key);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        String key = RedisKeys.getTenantKey();
        redisUtils.delete(key);
    }

    public void setDatasourceCache(List<SysTenantDataSourceDTO> list) {
        String key = RedisKeys.getTenantDatasourceKey();
        redisUtils.set(key, list, RedisUtils.NOT_EXPIRE);
    }

    public List<SysTenantDataSourceDTO> getDatasourceCache() {
        String key = RedisKeys.getTenantDatasourceKey();
        return (List<SysTenantDataSourceDTO>) redisUtils.get(key);
    }
}
