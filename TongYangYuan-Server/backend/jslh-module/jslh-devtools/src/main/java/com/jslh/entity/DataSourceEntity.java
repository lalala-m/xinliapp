/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import lombok.Data;

import java.util.Date;

/**
 * 数据源管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("gen_datasource")
public class DataSourceEntity {
	/**
	 * id
	 */
	@TableId
	private Long id;
    /**
     * 数据库类型 MySQL、Oracle、SQLServer、PostgreSQL、DM DBMS
     */
	private String dbType;
    /**
     * 连接名
     */
	private String connName;
    /**
     * URL
     */
	private String connUrl;
    /**
     * 用户名
     */
	private String username;
    /**
     * 密码
     */
	private String password;
    /**
     * 状态  0：启用   1：禁用
     */
	private Integer status;
	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date createDate;
}
