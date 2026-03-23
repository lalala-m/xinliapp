/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 创建菜单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface GeneratorMenuDao {

    void generatorMenu(Map<String, Object> params);

    void generatorMenuLanguage(Map<String, Object> params);
}
