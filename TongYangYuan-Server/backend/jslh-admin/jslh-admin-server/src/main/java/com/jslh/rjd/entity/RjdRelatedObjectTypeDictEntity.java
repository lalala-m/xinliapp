package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.util.Date;

/**
* 关联对象类型字典表
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_related_object_type_dict")
public class RjdRelatedObjectTypeDictEntity {
private static final long serialVersionUID = 1L;

			/**
			* 关联对象类型ID
			*/
			@TableId
		private Integer typeId;
			/**
			* 关联对象类型名称
			*/
		private String typeName;
			/**
			* 类型描述
			*/
		private String typeDesc;
			/**
			* 对应基础数据表名
			*/
		private String sourceTable;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 状态 0：停用 1：正常
			*/
		private Integer status;
			/**
			* 创建者（关联sys_user.id）
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long creator;
			/**
			* 创建时间
			*/
			@TableField(fill = FieldFill.INSERT)
		private Date createDate;
			/**
			* 更新者（关联sys_user.id）
			*/
			@TableField(fill = FieldFill.INSERT_UPDATE)
		private Long updater;
			/**
			* 更新时间
			*/
			@TableField(fill = FieldFill.INSERT_UPDATE)
		private Date updateDate;
			/**
			* 删除标识  0：未删除    1：删除
			*/
		private Integer delFlag;
}
