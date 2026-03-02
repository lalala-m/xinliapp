/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service.impl;

import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.dao.TableFieldDao;
import com.jslh.entity.TableFieldEntity;
import com.jslh.service.TableFieldService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 表
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class TableFieldServiceImpl extends BaseServiceImpl<TableFieldDao, TableFieldEntity> implements TableFieldService {

    @Override
    public List<TableFieldEntity> getByTableName(String tableName) {
        return baseDao.getByTableName(tableName);
    }

    @Override
    public void deleteByTableName(String tableName) {
        baseDao.deleteByTableName(tableName);
    }

    @Override
    public void deleteBatchTableIds(Long[] tableIds) {
        baseDao.deleteBatchTableIds(tableIds);
    }

}
