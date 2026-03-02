/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */
package com.jslh.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.dto.StorageDTO;
import com.jslh.entity.StorageEntity;

/**
 * 库存表
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface StorageService extends CrudService<StorageEntity, StorageDTO> {

    /**
     * 减库存
     *
     * @param commodityCode 商品代码
     * @param count         数量
     */
    void deduct(String commodityCode, int count);

}
