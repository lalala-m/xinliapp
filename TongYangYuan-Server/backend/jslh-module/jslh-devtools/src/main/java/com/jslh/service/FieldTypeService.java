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
import com.jslh.entity.FieldTypeEntity;

import java.util.Map;
import java.util.Set;

/**
 * 字段类型管理
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface FieldTypeService extends BaseService<FieldTypeEntity> {
    PageData<FieldTypeEntity> page(Map<String, Object> params);

    Map<String, FieldTypeEntity> getMap();

    /**
     * 根据tableId，获取包列表
     */
    Set<String> getPackageListByTableId(Long tableId);

    Set<String> list();
}
