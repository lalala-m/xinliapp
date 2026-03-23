package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdRelatedObjectTypeDictDao;
import com.jslh.rjd.dto.RjdRelatedObjectTypeDictDTO;
import com.jslh.rjd.entity.RjdRelatedObjectTypeDictEntity;
import com.jslh.rjd.service.RjdRelatedObjectTypeDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 关联对象类型字典表
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdRelatedObjectTypeDictServiceImpl extends CrudServiceImpl<RjdRelatedObjectTypeDictDao, RjdRelatedObjectTypeDictEntity, RjdRelatedObjectTypeDictDTO> implements RjdRelatedObjectTypeDictService {

    @Override
    public QueryWrapper<RjdRelatedObjectTypeDictEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdRelatedObjectTypeDictEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
