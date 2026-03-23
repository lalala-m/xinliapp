package com.jslh.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jslh.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("sys_tenant_datasource")
public class SysTenantDataSourceEntity extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/**
	* 名称
	*/
	private String name;
	/**
	* 驱动
	*/
	private String driverClassName;
	/**
	* URL
	*/
	private String url;
	/**
	* 用户名
	*/
	private String username;
	/**
	* 密码
	*/
	private String password;
	/**
	* 更新者
	*/
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Long updater;
	/**
	* 更新时间
	*/
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateDate;
}
