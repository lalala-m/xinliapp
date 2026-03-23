package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdEmployeeCommuteRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 管理人员通勤记录表（工时统计专用）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdEmployeeCommuteRecordDao extends BaseDao<RjdEmployeeCommuteRecordEntity> {

}
