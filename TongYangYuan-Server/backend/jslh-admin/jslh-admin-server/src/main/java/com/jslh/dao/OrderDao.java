/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
* 订单
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface OrderDao extends BaseDao<OrderEntity> {

    /**
     * 支付成功
     */
    int paySuccess(@Param("orderId") Long orderId, @Param("status") Integer status, @Param("payAt") Date payAt);

    OrderEntity getByOrderId(Long orderId);
}
