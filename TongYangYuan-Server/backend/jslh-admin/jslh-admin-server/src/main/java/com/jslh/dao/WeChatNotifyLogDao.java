/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.dao;


import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.WeChatNotifyLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信支付回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface WeChatNotifyLogDao extends BaseDao<WeChatNotifyLogEntity> {

}
