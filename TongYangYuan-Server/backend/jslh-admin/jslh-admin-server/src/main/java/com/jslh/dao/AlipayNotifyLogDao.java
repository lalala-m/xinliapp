/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.AlipayNotifyLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 支付宝回调日志
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface AlipayNotifyLogDao extends BaseDao<AlipayNotifyLogEntity> {

}
