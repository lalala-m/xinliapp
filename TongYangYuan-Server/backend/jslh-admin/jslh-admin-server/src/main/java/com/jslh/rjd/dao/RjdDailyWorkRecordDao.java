package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdDailyWorkRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 精铸行业岗位日结单主表（含耗时分摊校验）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdDailyWorkRecordDao extends BaseDao<RjdDailyWorkRecordEntity> {

}
