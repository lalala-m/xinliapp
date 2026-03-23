package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdDailyWorkRecordDao;
import com.jslh.rjd.dto.RjdDailyWorkRecordDTO;
import com.jslh.rjd.entity.RjdDailyWorkRecordEntity;
import com.jslh.rjd.service.RjdDailyWorkRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 精铸行业岗位日结单主表（含耗时分摊校验）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdDailyWorkRecordServiceImpl extends CrudServiceImpl<RjdDailyWorkRecordDao, RjdDailyWorkRecordEntity, RjdDailyWorkRecordDTO> implements RjdDailyWorkRecordService {

    @Override
    public QueryWrapper<RjdDailyWorkRecordEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdDailyWorkRecordEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
