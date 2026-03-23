package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdCompanyAffairInfoDao;
import com.jslh.rjd.dto.RjdCompanyAffairInfoDTO;
import com.jslh.rjd.entity.RjdCompanyAffairInfoEntity;
import com.jslh.rjd.service.RjdCompanyAffairInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 公司事务基础表（同步/录入）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdCompanyAffairInfoServiceImpl extends CrudServiceImpl<RjdCompanyAffairInfoDao, RjdCompanyAffairInfoEntity, RjdCompanyAffairInfoDTO> implements RjdCompanyAffairInfoService {

    @Override
    public QueryWrapper<RjdCompanyAffairInfoEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdCompanyAffairInfoEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
