package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.util.Date;

/**
* 公司事务基础表（同步/录入）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_company_affair_info")
public class RjdCompanyAffairInfoEntity {
private static final long serialVersionUID = 1L;

			/**
			* 事务ID（自增）
			*/
			@TableId
		private Long affairId;
			/**
			* 事务名称（如成本内控会议、安全培训等）
			*/
		private String affairName;
			/**
			* 事务类型（会议/培训/内控/其他）
			*/
		private String affairType;
			/**
			* 事务发生日期
			*/
		private Date affairDate;
			/**
			* 关联部门ID（关联sys_dept.id）
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long deptId;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 状态 0：停用 1：正常
			*/
		private Integer status;
			/**
			* 从原系统同步的时间（无则填NULL）
			*/
		private Date syncTime;
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
