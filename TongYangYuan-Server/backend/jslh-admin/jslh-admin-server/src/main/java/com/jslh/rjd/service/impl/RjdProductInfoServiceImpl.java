package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdProductInfoDao;
import com.jslh.rjd.dto.RjdProductInfoDTO;
import com.jslh.rjd.entity.RjdProductInfoEntity;
import com.jslh.rjd.service.RjdProductInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 产品基础表（同步原系统）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdProductInfoServiceImpl extends CrudServiceImpl<RjdProductInfoDao, RjdProductInfoEntity, RjdProductInfoDTO> implements RjdProductInfoService {

    @Override
    public QueryWrapper<RjdProductInfoEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdProductInfoEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
