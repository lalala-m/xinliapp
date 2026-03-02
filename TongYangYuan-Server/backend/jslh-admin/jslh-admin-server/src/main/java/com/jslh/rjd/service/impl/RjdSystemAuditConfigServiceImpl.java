package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdSystemAuditConfigDao;
import com.jslh.rjd.dto.RjdSystemAuditConfigDTO;
import com.jslh.rjd.entity.RjdSystemAuditConfigEntity;
import com.jslh.rjd.service.RjdSystemAuditConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 日结单系统审核配置表（精细化规则）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdSystemAuditConfigServiceImpl extends CrudServiceImpl<RjdSystemAuditConfigDao, RjdSystemAuditConfigEntity, RjdSystemAuditConfigDTO> implements RjdSystemAuditConfigService {

    @Override
    public QueryWrapper<RjdSystemAuditConfigEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdSystemAuditConfigEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
