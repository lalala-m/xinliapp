package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.math.BigDecimal;
	import java.util.Date;
	import com.jslh.commons.mybatis.entity.BaseEntity;

/**
* 精铸行业岗位日结单主表（含耗时分摊校验）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_daily_work_record")
public class RjdDailyWorkRecordEntity extends BaseEntity {
private static final long serialVersionUID = 1L;

			/**
			* 岗位ID（关联sys_post.id）
			*/
		private Long postId;
			/**
			* 岗位名称（冗余存储）
			*/
		private String postName;
			/**
			* 所属部门ID（关联sys_dept.id）
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long deptId;
			/**
			* 所属部门名称（冗余存储）
			*/
		private String deptName;
			/**
			* 填报日期
			*/
		private Date workDate;
			/**
			* 工作事项分类（关联rjd_post_work_item.work_item_name）
			*/
		private String workItemType;
			/**
			* 关联事项类型ID（关联rjd_related_object_type_dict.type_id）
			*/
		private Integer relatedTypeId;
			/**
			* 关联事项类型名称
			*/
		private String relatedTypeName;
			/**
			* 关联事项来源表
			*/
		private String relatedSourceTable;
			/**
			* 关联事项名称
			*/
		private String relatedObjectName;
			/**
			* 填报关联对象
			*/
		private String relatedObject;
			/**
			* 填报内容
			*/
		private String fillContent;
			/**
			* 总耗时（小时）
			*/
		private BigDecimal workingHours;
			/**
			* 关联对象分摊耗时总和（系统自动计算）
			*/
		private BigDecimal allocatedHoursSum;
			/**
			* 分摊校验状态：0=未校验 1=通过 2=失败
			*/
		private Integer allocatedCheckStatus;
			/**
			* 驳回后修改次数
			*/
		private Integer modifyCount;
			/**
			* 最新驳回原因
			*/
		private String latestRejectReason;
			/**
			* 通勤实际工时（冗余自rjd_employee_commute_record）
			*/
		private BigDecimal commuteActualHours;
			/**
			* 系统审核状态：0=待审核 1=通过 2=驳回
			*/
		private Integer systemAuditStatus;
			/**
			* 人工审核状态：0=待审核 1=通过 2=驳回
			*/
		private Integer manualAuditStatus;
			/**
			* 当前审核节点：system=系统 manual=人工
			*/
		private String latestAuditNode;
			/**
			* 填报人ID（关联sys_user.id）
			*/
		private Long fillerId;
			/**
			* 填报人姓名（冗余存储）
			*/
		private String fillerName;
			/**
			* 填报时间
			*/
		private Date fillDatetime;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 状态：0=停用 1=正常
			*/
		private Integer status;
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
			/**
			* 删除标识：0=未删 1=已删
			*/
		private Integer delFlag;
}
