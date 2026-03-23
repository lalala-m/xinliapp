/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.page.PageData;
import com.jslh.dao.DataSourceDao;
import com.jslh.entity.DataSourceEntity;
import com.jslh.service.DataSourceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * 数据源管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class DataSourceServiceImpl extends BaseServiceImpl<DataSourceDao, DataSourceEntity> implements DataSourceService {

    @Override
    public PageData<DataSourceEntity> page(Map<String, Object> params) {
        IPage<DataSourceEntity> page = baseDao.selectPage(
            getPage(params, Constant.CREATE_DATE, false),
            getWrapper(params)
        );
        return new PageData<>(page.getRecords(), page.getTotal());
    }

    private QueryWrapper<DataSourceEntity> getWrapper(Map<String, Object> params){
        String connName = (String)params.get("connName");
        String dbType = (String)params.get("dbType");

        QueryWrapper<DataSourceEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(connName), "conn_name", connName);
        wrapper.eq(StringUtils.isNotEmpty(dbType), "db_type", dbType);
        return wrapper;
    }

    @Override
    public List<DataSourceEntity> list() {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);

        return baseDao.selectList(wrapper);
    }

}
