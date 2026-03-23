package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.SysTenantDataSourceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysTenantDataSourceDao extends BaseDao<SysTenantDataSourceEntity> {

}
