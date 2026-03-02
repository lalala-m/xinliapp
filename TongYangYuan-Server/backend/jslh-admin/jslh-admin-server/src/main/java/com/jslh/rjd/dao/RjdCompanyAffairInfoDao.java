package com.jslh.rjd.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.rjd.entity.RjdCompanyAffairInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 公司事务基础表（同步/录入）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Mapper
public interface RjdCompanyAffairInfoDao extends BaseDao<RjdCompanyAffairInfoEntity> {

}
