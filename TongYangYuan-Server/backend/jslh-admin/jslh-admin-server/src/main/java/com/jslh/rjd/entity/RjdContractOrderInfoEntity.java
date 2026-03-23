package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.math.BigDecimal;
	import java.util.Date;

/**
* 合同订单基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_contract_order_info")
public class RjdContractOrderInfoEntity {
private static final long serialVersionUID = 1L;

			/**
			* 订单ID（与原系统一致）
			*/
			@TableId
		private Long orderId;
			/**
			* 订单编号（核心标识）
			*/
		private String orderCode;
			/**
			* 订单类型（销售订单/生产订单/采购订单）
			*/
		private String orderType;
			/**
			* 关联产品ID（关联rjd_product_info.product_id）
			*/
		private Long productId;
			/**
			* 关联客户ID（关联rjd_customer_info.customer_id）
			*/
		private Long customerId;
			/**
			* 订单金额
			*/
		private BigDecimal orderAmount;
			/**
			* 订单日期
			*/
		private Date orderDate;
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
