package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdCustomerInfoDao;
import com.jslh.rjd.dto.RjdCustomerInfoDTO;
import com.jslh.rjd.entity.RjdCustomerInfoEntity;
import com.jslh.rjd.service.RjdCustomerInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 客户基础表（同步原系统）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdCustomerInfoServiceImpl extends CrudServiceImpl<RjdCustomerInfoDao, RjdCustomerInfoEntity, RjdCustomerInfoDTO> implements RjdCustomerInfoService {

    @Override
    public QueryWrapper<RjdCustomerInfoEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdCustomerInfoEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
