/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.form.service;

import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.form.dto.CorrectionDTO;
import com.jslh.form.entity.CorrectionEntity;

/**
 * 转正申请
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface CorrectionService extends BaseService<CorrectionEntity> {

    CorrectionDTO get(String instanceId);

    void save(CorrectionDTO dto);
}
