package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface ProductDao extends BaseDao<ProductEntity> {

}
