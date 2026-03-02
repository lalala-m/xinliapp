/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.page.PageData;
import com.jslh.dao.OssDao;
import com.jslh.entity.OssEntity;
import com.jslh.service.OssService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OssServiceImpl extends BaseServiceImpl<OssDao, OssEntity> implements OssService {

	@Override
	public PageData<OssEntity> page(Map<String, Object> params) {
		IPage<OssEntity> page = baseDao.selectPage(
			getPage(params, Constant.CREATE_DATE, false),
			new QueryWrapper<>()
		);
		return getPageData(page, OssEntity.class);
	}
}
