package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 客户基础表（同步原系统）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdCustomerInfoExcel {
    @ExcelProperty(value = "客户ID（与原系统一致）", index = 0)
    private Long customerId;
    @ExcelProperty(value = "客户名称", index = 1)
    private String customerName;
    @ExcelProperty(value = "客户编码", index = 2)
    private String customerCode;
    @ExcelProperty(value = "对接人", index = 3)
    private String contactPerson;
    @ExcelProperty(value = "联系电话", index = 4)
    private String contactPhone;
    @ExcelProperty(value = "客户类型（如终端客户、经销商等）", index = 5)
    private String customerType;
    @ExcelProperty(value = "租户编码", index = 6)
    private Long tenantCode;
    @ExcelProperty(value = "状态 0：停用 1：正常", index = 7)
    private Integer status;
    @ExcelProperty(value = "从原系统同步的时间", index = 8)
    private Date syncTime;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 9)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 10)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 11)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 12)
    private Date updateDate;
    @ExcelProperty(value = "删除标识  0：未删除    1：删除", index = 13)
    private Integer delFlag;
}
