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
import com.jslh.dto.ScheduleJobDTO;
import com.jslh.entity.ScheduleJobEntity;

import java.util.Map;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface ScheduleJobService extends BaseService<ScheduleJobEntity> {

	PageData<ScheduleJobDTO> page(Map<String, Object> params);

	ScheduleJobDTO get(Long id);

	/**
	 * 保存定时任务
	 */
	void save(ScheduleJobDTO dto);

	/**
	 * 更新定时任务
	 */
	void update(ScheduleJobDTO dto);

	/**
	 * 批量删除定时任务
	 */
	void deleteBatch(Long[] ids);

	/**
	 * 批量更新定时任务状态
	 */
	int updateBatch(Long[] ids, int status);

	/**
	 * 立即执行
	 */
	void run(Long[] ids);

	/**
	 * 暂停运行
	 */
	void pause(Long[] ids);

	/**
	 * 恢复运行
	 */
	void resume(Long[] ids);
}
