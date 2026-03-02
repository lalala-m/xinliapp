package com.jslh.rjd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.rjd.dao.RjdPostWorkItemDao;
import com.jslh.rjd.dto.RjdPostWorkItemDTO;
import com.jslh.rjd.entity.RjdPostWorkItemEntity;
import com.jslh.rjd.service.RjdPostWorkItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 精铸行业岗位工作事项字典表
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Service
public class RjdPostWorkItemServiceImpl extends CrudServiceImpl<RjdPostWorkItemDao, RjdPostWorkItemEntity, RjdPostWorkItemDTO> implements RjdPostWorkItemService {

    @Override
    public QueryWrapper<RjdPostWorkItemEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<RjdPostWorkItemEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}
