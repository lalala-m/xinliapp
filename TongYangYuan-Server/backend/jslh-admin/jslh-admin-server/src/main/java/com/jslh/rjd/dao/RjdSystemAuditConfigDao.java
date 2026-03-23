package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdSystemAuditConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 日结单系统审核配置表（精细化规则）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdSystemAuditConfigDao extends BaseDao<RjdSystemAuditConfigEntity> {

}
