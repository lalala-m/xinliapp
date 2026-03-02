/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */
package com.jslh.dao;

import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.SysNoticeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* 通知管理
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface SysNoticeDao extends BaseDao<SysNoticeEntity> {
    /**
     * 获取被通知的用户列表
     */
    List<SysNoticeEntity> getNoticeUserList(Map<String, Object> params);

    /**
     * 获取我的通知列表
     */
    List<SysNoticeEntity> getMyNoticeList(Map<String, Object> params);
}
