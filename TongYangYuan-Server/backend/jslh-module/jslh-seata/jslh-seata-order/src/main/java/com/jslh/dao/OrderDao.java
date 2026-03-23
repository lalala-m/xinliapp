/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */
package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 订单
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface OrderDao extends BaseDao<OrderEntity> {

}
