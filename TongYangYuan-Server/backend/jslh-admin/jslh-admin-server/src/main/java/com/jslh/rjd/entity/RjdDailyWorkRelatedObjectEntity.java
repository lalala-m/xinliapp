package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.math.BigDecimal;
	import java.util.Date;
	import com.jslh.commons.mybatis.entity.BaseEntity;

/**
* 日结单-关联对象中间表（含耗时分摊）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_daily_work_related_object")
public class RjdDailyWorkRelatedObjectEntity extends BaseEntity {
private static final long serialVersionUID = 1L;

			/**
			* 关联日结单ID（rjd_daily_work_record.id）
			*/
		private Long dailyRecordId;
			/**
			* 关联对象类型ID（rjd_related_object_type_dict.type_id）
			*/
		private Integer relatedTypeId;
			/**
			* 关联对象ID（产品/客户/订单/事务ID）
			*/
		private Long relatedObjectId;
			/**
			* 关联对象名称（冗余存储）
			*/
		private String relatedObjectName;
			/**
			* 分摊到该对象的耗时（小时）
			*/
		private BigDecimal allocatedHours;
			/**
			* 分摊比例（%）
			*/
		private BigDecimal allocatedRatio;
			/**
			* 分摊备注
			*/
		private String allocatedRemark;
			/**
			* 租户编码
			*/
		private Long tenantCode;
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
