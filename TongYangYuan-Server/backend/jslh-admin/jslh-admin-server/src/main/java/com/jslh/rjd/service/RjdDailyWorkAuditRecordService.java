package com.jslh.rjd.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.rjd.dto.RjdDailyWorkAuditRecordDTO;
import com.jslh.rjd.entity.RjdDailyWorkAuditRecordEntity;

/**
 * 精铸行业岗位日结单审核记录表（含分摊校验）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
public interface RjdDailyWorkAuditRecordService extends CrudService<RjdDailyWorkAuditRecordEntity, RjdDailyWorkAuditRecordDTO> {

}
