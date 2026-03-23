/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.config.DataSourceInfo;
import com.jslh.entity.MenuEntity;
import com.jslh.entity.TableFieldEntity;
import com.jslh.entity.TableInfoEntity;

import java.util.List;

/**
 * 代码生成
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface GeneratorService {

    DataSourceInfo getDataSourceInfo(Long datasourceId);

    void datasourceTable(TableInfoEntity tableInfo);

    void updateTableField(Long tableId, List<TableFieldEntity> tableFieldList);

    void generatorCode(TableInfoEntity tableInfo);

    void generatorMenu(MenuEntity menu);
}
