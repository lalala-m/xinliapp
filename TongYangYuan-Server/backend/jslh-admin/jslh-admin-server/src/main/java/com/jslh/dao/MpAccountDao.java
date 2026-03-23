package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.MpAccountEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 公众号账号管理
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface MpAccountDao extends BaseDao<MpAccountEntity> {

}
