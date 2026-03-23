package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 日结单-关联对象中间表（含耗时分摊）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdDailyWorkRelatedObjectExcel {
    @ExcelProperty(value = "主键ID", index = 0)
    private Long id;
    @ExcelProperty(value = "关联日结单ID（rjd_daily_work_record.id）", index = 1)
    private Long dailyRecordId;
    @ExcelProperty(value = "关联对象类型ID（rjd_related_object_type_dict.type_id）", index = 2)
    private Integer relatedTypeId;
    @ExcelProperty(value = "关联对象ID（产品/客户/订单/事务ID）", index = 3)
    private Long relatedObjectId;
    @ExcelProperty(value = "关联对象名称（冗余存储）", index = 4)
    private String relatedObjectName;
    @ExcelProperty(value = "分摊到该对象的耗时（小时）", index = 5)
    private BigDecimal allocatedHours;
    @ExcelProperty(value = "分摊比例（%）", index = 6)
    private BigDecimal allocatedRatio;
    @ExcelProperty(value = "分摊备注", index = 7)
    private String allocatedRemark;
    @ExcelProperty(value = "租户编码", index = 8)
    private Long tenantCode;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 9)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 10)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 11)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 12)
    private Date updateDate;
    @ExcelProperty(value = "删除标识：0=未删 1=已删", index = 13)
    private Integer delFlag;
}
