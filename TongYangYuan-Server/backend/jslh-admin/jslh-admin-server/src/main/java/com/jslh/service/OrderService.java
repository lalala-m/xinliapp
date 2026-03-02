/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.dto.OrderDTO;
import com.jslh.entity.OrderEntity;

/**
 * 订单
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface OrderService extends CrudService<OrderEntity, OrderDTO> {


    OrderEntity getByOrderId(Long orderId);

    /**
     * 支付成功
     * @param order 订单
     */
    void paySuccess(OrderEntity order);
}
