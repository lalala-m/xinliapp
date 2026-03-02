package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.math.BigDecimal;
	import java.util.Date;

/**
* 日结单系统审核配置表（精细化规则）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_system_audit_config")
public class RjdSystemAuditConfigEntity {
private static final long serialVersionUID = 1L;

			/**
			* 配置ID
			*/
			@TableId
		private Long configId;
			/**
			* 配置名称（如：生产管理人员日结单审核规则）
			*/
		private String configName;
			/**
			* 适用岗位ID（sys_post.id，NULL=全岗位）
			*/
		private Long postId;
			/**
			* 适用岗位名称
			*/
		private String postName;
			/**
			* 适用部门ID（sys_dept.id，NULL=全部门）
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long deptId;
			/**
			* 适用部门名称
			*/
		private String deptName;
			/**
			* 必填字段（逗号分隔）
			*/
		private String requiredFields;
			/**
			* 填报耗时最小值（小时）
			*/
		private BigDecimal workingHoursMin;
			/**
			* 填报耗时最大值（小时）
			*/
		private BigDecimal workingHoursMax;
			/**
			* 关联对象校验：0=不校验 1=校验
			*/
		private Integer relatedObjectCheck;
			/**
			* 工时匹配：0=不校验 1=填报≤通勤
			*/
		private Integer actualHoursMatch;
			/**
			* 自动通过：0=否 1=是
			*/
		private Integer autoPass;
			/**
			* 填报内容最小字数（0=不限制）
			*/
		private Integer fillContentMinLength;
			/**
			* 分摊校验：0=不校验 1=总和=总耗时
			*/
		private Integer allocatedCheck;
			/**
			* 驳回后最大修改次数（0=不限制）
			*/
		private Integer modifyLimit;
			/**
			* 关联对象最少数量（0=不限制）
			*/
		private Integer relatedObjectMinCount;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 状态：0=停用 1=启用
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
			* 删除标识：0=未删 1=已删
			*/
		private Integer delFlag;
}
