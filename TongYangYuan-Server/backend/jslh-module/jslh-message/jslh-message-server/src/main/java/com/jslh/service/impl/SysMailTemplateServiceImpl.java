/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jslh.commons.mybatis.service.impl.CrudServiceImpl;
import com.jslh.commons.tools.exception.ErrorCode;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.commons.tools.utils.JsonUtils;
import com.jslh.dao.SysMailTemplateDao;
import com.jslh.dto.SysMailTemplateDTO;
import com.jslh.email.EmailUtils;
import com.jslh.entity.SysMailTemplateEntity;
import com.jslh.service.SysMailTemplateService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SysMailTemplateServiceImpl extends CrudServiceImpl<SysMailTemplateDao, SysMailTemplateEntity, SysMailTemplateDTO> implements SysMailTemplateService {
    @Resource
    private EmailUtils emailUtils;

    @Override
    public QueryWrapper<SysMailTemplateEntity> getWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");

        QueryWrapper<SysMailTemplateEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(name), "name", name);

        return wrapper;
    }

    @Override
    public boolean sendMail(Long id, String mailTo, String mailCc, String params) throws Exception {
        Map<String, Object> map = null;
        try {
            if (StringUtils.isNotEmpty(params)) {
                map = JsonUtils.parseObject(params, Map.class);
            }
        } catch (Exception e) {
            throw new RenException(ErrorCode.JSON_FORMAT_ERROR);
        }
        String[] to = new String[]{mailTo};
        String[] cc = StringUtils.isBlank(mailCc) ? null : new String[]{mailCc};

        return emailUtils.sendMail(id, to, cc, map);
    }
}
