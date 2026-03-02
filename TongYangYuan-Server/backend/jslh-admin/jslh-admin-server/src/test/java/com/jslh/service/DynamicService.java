/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.commons.dynamic.datasource.annotation.DataSource;
import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.dao.SysUserDao;
import com.jslh.entity.SysUserEntity;

/**
 * 测试多数据源
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.1.0
 */
@DataSource("slave2")
public class DynamicService extends BaseServiceImpl<SysUserDao, SysUserEntity> {
}
