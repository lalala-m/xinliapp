package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 合同订单基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "合同订单基础表（同步原系统）")
public class RjdContractOrderInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID（与原系统一致）")
    private Long orderId;
    @Schema(description = "订单编号（核心标识）")
    private String orderCode;
    @Schema(description = "订单类型（销售订单/生产订单/采购订单）")
    private String orderType;
    @Schema(description = "关联产品ID（关联rjd_product_info.product_id）")
    private Long productId;
    @Schema(description = "关联客户ID（关联rjd_customer_info.customer_id）")
    private Long customerId;
    @Schema(description = "订单金额")
    private BigDecimal orderAmount;
    @Schema(description = "订单日期")
    private Date orderDate;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态 0：停用 1：正常")
    private Integer status;
    @Schema(description = "从原系统同步的时间")
    private Date syncTime;
    @Schema(description = "创建者（关联sys_user.id）")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者（关联sys_user.id）")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;
    @Schema(description = "删除标识  0：未删除    1：删除")
    private Integer delFlag;

}
