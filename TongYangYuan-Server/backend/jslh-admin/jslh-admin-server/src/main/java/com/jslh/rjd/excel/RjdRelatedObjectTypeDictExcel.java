package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 关联对象类型字典表
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdRelatedObjectTypeDictExcel {
    @ExcelProperty(value = "关联对象类型ID", index = 0)
    private Integer typeId;
    @ExcelProperty(value = "关联对象类型名称", index = 1)
    private String typeName;
    @ExcelProperty(value = "类型描述", index = 2)
    private String typeDesc;
    @ExcelProperty(value = "对应基础数据表名", index = 3)
    private String sourceTable;
    @ExcelProperty(value = "租户编码", index = 4)
    private Long tenantCode;
    @ExcelProperty(value = "状态 0：停用 1：正常", index = 5)
    private Integer status;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 6)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 7)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 8)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 9)
    private Date updateDate;
    @ExcelProperty(value = "删除标识  0：未删除    1：删除", index = 10)
    private Integer delFlag;
}
