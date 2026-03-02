package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdDailyWorkAuditRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 精铸行业岗位日结单审核记录表（含分摊校验）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdDailyWorkAuditRecordDao extends BaseDao<RjdDailyWorkAuditRecordEntity> {

}
