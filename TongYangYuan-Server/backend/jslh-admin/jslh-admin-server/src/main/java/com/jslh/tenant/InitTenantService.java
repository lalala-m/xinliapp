/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.tenant;

import com.jslh.commons.tools.utils.ConvertUtils;
import com.jslh.dao.SysTenantDao;
import com.jslh.dao.SysTenantDataSourceDao;
import com.jslh.entity.SysTenantDataSourceEntity;
import com.jslh.entity.SysTenantEntity;
import com.jslh.tenant.dto.SysTenantDataSourceDTO;
import com.jslh.tenant.dto.SysTenantListDTO;
import com.jslh.tenant.redis.SysTenantRedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化租户
 *
 * @author Mark sunlightcs@gmail.com
 */
public class InitTenantService {
    private final SysTenantDataSourceDao sysTenantDataSourceDao;
    private final SysTenantDao sysTenantDao;
    private final SysTenantRedis sysTenantRedis;

    public InitTenantService(SysTenantDataSourceDao sysTenantDataSourceDao, SysTenantDao sysTenantDao, SysTenantRedis sysTenantRedis) {
        this.sysTenantDataSourceDao = sysTenantDataSourceDao;
        this.sysTenantDao = sysTenantDao;
        this.sysTenantRedis = sysTenantRedis;

        initTenantDataSource();
        initTenantList();
    }

    // 初始化租户数据源到redis
    private void initTenantDataSource() {
        List<SysTenantDataSourceEntity> dataSourceList = sysTenantDataSourceDao.selectList(null);

        sysTenantRedis.setDatasourceCache(ConvertUtils.sourceToTarget(dataSourceList, SysTenantDataSourceDTO.class));
    }

    // 初始化租户列表到redis
    private void initTenantList() {
        List<SysTenantListDTO> dtoList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        List<SysTenantEntity> list = sysTenantDao.getList(params);
        for (SysTenantEntity entity : list) {
            SysTenantListDTO dto = new SysTenantListDTO();
            dto.setTenantCode(entity.getId());
            dto.setTenantName(entity.getTenantName());
            dto.setTenantDomain(entity.getTenantDomain());
            dto.setTenantMode(entity.getTenantMode());
            dto.setDatasourceId(entity.getDatasourceId());
            dto.setStatus(entity.getStatus());

            dtoList.add(dto);
        }

        sysTenantRedis.setCache(dtoList);
    }
}
