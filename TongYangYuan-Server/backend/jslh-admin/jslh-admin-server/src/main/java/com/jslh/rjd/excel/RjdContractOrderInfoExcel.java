package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同订单基础表（同步原系统）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdContractOrderInfoExcel {
    @ExcelProperty(value = "订单ID（与原系统一致）", index = 0)
    private Long orderId;
    @ExcelProperty(value = "订单编号（核心标识）", index = 1)
    private String orderCode;
    @ExcelProperty(value = "订单类型（销售订单/生产订单/采购订单）", index = 2)
    private String orderType;
    @ExcelProperty(value = "关联产品ID（关联rjd_product_info.product_id）", index = 3)
    private Long productId;
    @ExcelProperty(value = "关联客户ID（关联rjd_customer_info.customer_id）", index = 4)
    private Long customerId;
    @ExcelProperty(value = "订单金额", index = 5)
    private BigDecimal orderAmount;
    @ExcelProperty(value = "订单日期", index = 6)
    private Date orderDate;
    @ExcelProperty(value = "租户编码", index = 7)
    private Long tenantCode;
    @ExcelProperty(value = "状态 0：停用 1：正常", index = 8)
    private Integer status;
    @ExcelProperty(value = "从原系统同步的时间", index = 9)
    private Date syncTime;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 10)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 11)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 12)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 13)
    private Date updateDate;
    @ExcelProperty(value = "删除标识  0：未删除    1：删除", index = 14)
    private Integer delFlag;
}
