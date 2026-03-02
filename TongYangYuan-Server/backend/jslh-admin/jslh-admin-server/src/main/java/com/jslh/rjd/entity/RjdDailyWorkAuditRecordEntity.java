package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.util.Date;
	import com.jslh.commons.mybatis.entity.BaseEntity;

/**
* 精铸行业岗位日结单审核记录表（含分摊校验）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_daily_work_audit_record")
public class RjdDailyWorkAuditRecordEntity extends BaseEntity {
private static final long serialVersionUID = 1L;

			/**
			* 关联日结单ID
			*/
		private Long dailyRecordId;
			/**
			* 审核节点：system=系统 manual=人工
			*/
		private String auditNode;
			/**
			* 审核顺序：1=系统 2=人工
			*/
		private Integer auditOrder;
			/**
			* 关联系统审核配置ID（仅system节点）
			*/
		private Long systemConfigId;
			/**
			* 系统审核规则详情
			*/
		private String auditRuleDetail;
			/**
			* 分摊校验详情
			*/
		private String allocatedAuditDetail;
			/**
			* 审核类型：1=系统 2=人工
			*/
		private Integer auditType;
			/**
			* 审核状态：0=待审核 1=通过 2=驳回
			*/
		private Integer auditStatus;
			/**
			* 审核人ID（system=0，manual=sys_user.id）
			*/
		private Long auditorId;
			/**
			* 审核人姓名（system=系统）
			*/
		private String auditorName;
			/**
			* 审核完成时间
			*/
		private Date auditTime;
			/**
			* 审核备注
			*/
		private String auditRemark;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 删除标识：0=未删 1=已删
			*/
		private Integer delFlag;
}
