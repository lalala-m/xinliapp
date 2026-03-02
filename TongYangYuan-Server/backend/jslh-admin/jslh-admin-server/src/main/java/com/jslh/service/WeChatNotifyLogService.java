/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.dto.WeChatNotifyLogDTO;
import com.jslh.entity.WeChatNotifyLogEntity;

/**
 * 微信支付回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface WeChatNotifyLogService extends CrudService<WeChatNotifyLogEntity, WeChatNotifyLogDTO> {

}
