package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdDailyWorkRelatedObjectDao;
import com.jslh.rjd.dto.RjdDailyWorkRelatedObjectDTO;
import com.jslh.rjd.entity.RjdDailyWorkRelatedObjectEntity;
import com.jslh.rjd.service.RjdDailyWorkRelatedObjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 日结单-关联对象中间表（含耗时分摊）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdDailyWorkRelatedObjectServiceImpl extends CrudServiceImpl<RjdDailyWorkRelatedObjectDao, RjdDailyWorkRelatedObjectEntity, RjdDailyWorkRelatedObjectDTO> implements RjdDailyWorkRelatedObjectService {

    @Override
    public QueryWrapper<RjdDailyWorkRelatedObjectEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdDailyWorkRelatedObjectEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
