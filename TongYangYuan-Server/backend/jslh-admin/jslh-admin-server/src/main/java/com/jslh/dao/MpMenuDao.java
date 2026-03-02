package com.jslh.dao;


import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.MpMenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 公众号自定义菜单
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface MpMenuDao extends BaseDao<MpMenuEntity> {

}
