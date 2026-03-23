package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 产品基础表（同步原系统）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdProductInfoExcel {
    @ExcelProperty(value = "产品ID（与原系统一致）", index = 0)
    private Long productId;
    @ExcelProperty(value = "产品型号（核心标识）", index = 1)
    private String productModel;
    @ExcelProperty(value = "产品名称", index = 2)
    private String productName;
    @ExcelProperty(value = "产品规格", index = 3)
    private String productSpec;
    @ExcelProperty(value = "产品类型（如精密铸件、模具等）", index = 4)
    private String productType;
    @ExcelProperty(value = "租户编码", index = 5)
    private Long tenantCode;
    @ExcelProperty(value = "状态 0：停用 1：正常", index = 6)
    private Integer status;
    @ExcelProperty(value = "从原系统同步的时间", index = 7)
    private Date syncTime;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 8)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 9)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 10)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 11)
    private Date updateDate;
    @ExcelProperty(value = "删除标识  0：未删除    1：删除", index = 12)
    private Integer delFlag;
}
