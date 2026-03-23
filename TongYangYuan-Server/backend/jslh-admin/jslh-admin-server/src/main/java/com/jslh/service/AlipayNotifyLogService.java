/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.dto.AlipayNotifyLogDTO;
import com.jslh.entity.AlipayNotifyLogEntity;

/**
 * 支付宝回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AlipayNotifyLogService extends CrudService<AlipayNotifyLogEntity, AlipayNotifyLogDTO> {

}
