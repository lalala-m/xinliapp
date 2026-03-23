package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdDailyWorkAuditRecordDao;
import com.jslh.rjd.dto.RjdDailyWorkAuditRecordDTO;
import com.jslh.rjd.entity.RjdDailyWorkAuditRecordEntity;
import com.jslh.rjd.service.RjdDailyWorkAuditRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 精铸行业岗位日结单审核记录表（含分摊校验）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdDailyWorkAuditRecordServiceImpl extends CrudServiceImpl<RjdDailyWorkAuditRecordDao, RjdDailyWorkAuditRecordEntity, RjdDailyWorkAuditRecordDTO> implements RjdDailyWorkAuditRecordService {

    @Override
    public QueryWrapper<RjdDailyWorkAuditRecordEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdDailyWorkAuditRecordEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
