package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdContractOrderInfoDao;
import com.jslh.rjd.dto.RjdContractOrderInfoDTO;
import com.jslh.rjd.entity.RjdContractOrderInfoEntity;
import com.jslh.rjd.service.RjdContractOrderInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 合同订单基础表（同步原系统）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdContractOrderInfoServiceImpl extends CrudServiceImpl<RjdContractOrderInfoDao, RjdContractOrderInfoEntity, RjdContractOrderInfoDTO> implements RjdContractOrderInfoService {

    @Override
    public QueryWrapper<RjdContractOrderInfoEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdContractOrderInfoEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
