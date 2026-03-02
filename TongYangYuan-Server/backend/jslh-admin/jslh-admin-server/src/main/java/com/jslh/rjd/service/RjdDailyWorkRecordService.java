package com.jslh.rjd.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.rjd.dto.RjdDailyWorkRecordDTO;
import com.jslh.rjd.entity.RjdDailyWorkRecordEntity;

/**
 * 精铸行业岗位日结单主表（含耗时分摊校验）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
public interface RjdDailyWorkRecordService extends CrudService<RjdDailyWorkRecordEntity, RjdDailyWorkRecordDTO> {

}
