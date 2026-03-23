/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.service;

import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.commons.tools.page.PageData;
import com.jslh.tenant.dto.SysTenantDTO;
import com.jslh.tenant.dto.SysTenantListDTO;
import com.jslh.entity.SysTenantEntity;

import java.util.List;
import java.util.Map;


/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface SysTenantService extends BaseService<SysTenantEntity> {

	PageData<SysTenantDTO> page(Map<String, Object> params);

	List<SysTenantListDTO> list();

	SysTenantDTO get(Long id);

	void save(SysTenantDTO dto);

	void update(SysTenantDTO dto);

	void delete(Long[] ids);

}
