/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.commons.tools.page.PageData;
import com.jslh.entity.DataSourceEntity;

import java.util.List;
import java.util.Map;

/**
 * 数据源管理
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface DataSourceService extends BaseService<DataSourceEntity> {

    PageData<DataSourceEntity> page(Map<String, Object> params);

    List<DataSourceEntity> list();
}
