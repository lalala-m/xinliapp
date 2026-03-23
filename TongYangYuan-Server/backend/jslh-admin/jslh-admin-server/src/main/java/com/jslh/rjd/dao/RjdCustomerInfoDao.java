package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdCustomerInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 客户基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdCustomerInfoDao extends BaseDao<RjdCustomerInfoEntity> {

}
