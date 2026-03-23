package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdEmployeeCommuteRecordDao;
import com.jslh.rjd.dto.RjdEmployeeCommuteRecordDTO;
import com.jslh.rjd.entity.RjdEmployeeCommuteRecordEntity;
import com.jslh.rjd.service.RjdEmployeeCommuteRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 管理人员通勤记录表（工时统计专用）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdEmployeeCommuteRecordServiceImpl extends CrudServiceImpl<RjdEmployeeCommuteRecordDao, RjdEmployeeCommuteRecordEntity, RjdEmployeeCommuteRecordDTO> implements RjdEmployeeCommuteRecordService {

    @Override
    public QueryWrapper<RjdEmployeeCommuteRecordEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdEmployeeCommuteRecordEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
