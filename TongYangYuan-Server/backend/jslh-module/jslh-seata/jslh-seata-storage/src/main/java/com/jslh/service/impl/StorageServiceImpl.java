/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.dao.StorageDao;
import com.jslh.dto.StorageDTO;
import com.jslh.entity.StorageEntity;
import com.jslh.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 库存表
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class StorageServiceImpl extends CrudServiceImpl<StorageDao, StorageEntity, StorageDTO> implements StorageService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(String commodityCode, int count) {
        int updateCount = baseDao.updateDeduct(commodityCode, count);
        if (updateCount == 0) {
            throw new RenException("库存数不足，请稍后再试！");
        }
    }

    @Override
    public QueryWrapper<StorageEntity> getWrapper(Map<String, Object> params) {
        return null;
    }

}
