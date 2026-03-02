/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.SysTenantEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysTenantDao extends BaseDao<SysTenantEntity> {

    List<SysTenantEntity> getList(Map<String, Object> params);

    SysTenantEntity getById(Long id);

    void deleteBatch(Long[] ids);
}
