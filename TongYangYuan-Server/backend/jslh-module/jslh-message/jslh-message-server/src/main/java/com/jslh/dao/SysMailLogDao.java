/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.SysMailLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件发送记录
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysMailLogDao extends BaseDao<SysMailLogEntity> {

}
