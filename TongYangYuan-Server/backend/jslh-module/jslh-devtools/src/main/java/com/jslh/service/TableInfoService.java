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
import com.jslh.entity.TableInfoEntity;

import java.util.Map;

/**
 * 表
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface TableInfoService extends BaseService<TableInfoEntity> {

    PageData<TableInfoEntity> page(Map<String, Object> params);

    TableInfoEntity getByTableName(String tableName);

    void deleteByTableName(String tableName);

    void deleteBatchIds(Long[] ids);
}
