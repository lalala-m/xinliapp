/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jslh.entity.BaseClassEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基类管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface BaseClassDao extends BaseMapper<BaseClassEntity> {

}
