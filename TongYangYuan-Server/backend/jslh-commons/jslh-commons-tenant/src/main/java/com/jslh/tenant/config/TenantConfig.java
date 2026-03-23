package com.jslh.tenant.config;

import com.jslh.tenant.redis.SysTenantRedis;
import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;

@Configuration
@ConditionalOnProperty(prefix = "jslh.tenant", value = "enable", havingValue = "true")
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfig {
    @Resource
    private TenantProperties tenantProperties;
    @Resource
    private PathMatcher pathMatcher;
    @Resource
    private SysTenantRedis sysTenantRedis;

    @Bean
    public InitTenantDataSource tenantInterceptor() {
        return new InitTenantDataSource();
    }

    @Bean
    public FilterRegistrationBean<TenantRequestFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantRequestFilter(tenantProperties, pathMatcher, sysTenantRedis));
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.addUrlPatterns("/*");
        registration.setName("tenantFilter");
        registration.setOrder(0);
        return registration;
    }
}
