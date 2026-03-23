package com.jslh.rjd.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.rjd.dto.RjdDailyWorkRelatedObjectDTO;
import com.jslh.rjd.entity.RjdDailyWorkRelatedObjectEntity;

/**
 * 日结单-关联对象中间表（含耗时分摊）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
public interface RjdDailyWorkRelatedObjectService extends CrudService<RjdDailyWorkRelatedObjectEntity, RjdDailyWorkRelatedObjectDTO> {

}
