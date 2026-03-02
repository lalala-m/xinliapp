package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdDailyWorkRelatedObjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 日结单-关联对象中间表（含耗时分摊）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdDailyWorkRelatedObjectDao extends BaseDao<RjdDailyWorkRelatedObjectEntity> {

}
