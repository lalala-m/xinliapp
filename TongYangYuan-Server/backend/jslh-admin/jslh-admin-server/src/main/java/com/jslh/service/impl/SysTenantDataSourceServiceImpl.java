package com.jslh.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.dynamic.datasource.config.DynamicDataSource;
import com.jslh.commons.dynamic.datasource.config.DynamicDataSourceFactory;
import com.jslh.commons.dynamic.datasource.properties.DataSourceProperties;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.dao.SysTenantDataSourceDao;
import com.jslh.entity.SysTenantDataSourceEntity;
import com.jslh.service.SysTenantDataSourceService;
import com.jslh.tenant.dto.SysTenantDataSourceDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class SysTenantDataSourceServiceImpl extends CrudServiceImpl<SysTenantDataSourceDao, SysTenantDataSourceEntity, SysTenantDataSourceDTO> implements SysTenantDataSourceService {
    @Resource
    private DynamicDataSource dynamicDataSource;

    @Override
    public QueryWrapper<SysTenantDataSourceEntity> getWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");

        QueryWrapper<SysTenantDataSourceEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(name), "name", name);

        return wrapper;
    }

    @Override
    public void save(SysTenantDataSourceDTO dto) {
        super.save(dto);

        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setDriverClassName(dto.getDriverClassName());
        dataSourceProperties.setUrl(dto.getUrl());
        dataSourceProperties.setUsername(dto.getUsername());
        dataSourceProperties.setPassword(dto.getPassword());
        DruidDataSource druidDataSource = DynamicDataSourceFactory.buildDruidDataSource(dataSourceProperties);

        if (!druidDataSource.isEnable()) {
            throw new RenException("数据源不可用，请仔细检查");
        }

        // 新增动态数据源
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(dto.getId() + "", druidDataSource);
        dynamicDataSource.addDataSources(dataSources);
    }
}
