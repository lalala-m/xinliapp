package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.util.Date;

/**
* 产品基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_product_info")
public class RjdProductInfoEntity {
private static final long serialVersionUID = 1L;

			/**
			* 产品ID（与原系统一致）
			*/
			@TableId
		private Long productId;
			/**
			* 产品型号（核心标识）
			*/
		private String productModel;
			/**
			* 产品名称
			*/
		private String productName;
			/**
			* 产品规格
			*/
		private String productSpec;
			/**
			* 产品类型（如精密铸件、模具等）
			*/
		private String productType;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 状态 0：停用 1：正常
			*/
		private Integer status;
			/**
			* 从原系统同步的时间
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
