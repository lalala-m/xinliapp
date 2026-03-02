package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 公司事务基础表（同步/录入）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdCompanyAffairInfoExcel {
    @ExcelProperty(value = "事务ID（自增）", index = 0)
    private Long affairId;
    @ExcelProperty(value = "事务名称（如成本内控会议、安全培训等）", index = 1)
    private String affairName;
    @ExcelProperty(value = "事务类型（会议/培训/内控/其他）", index = 2)
    private String affairType;
    @ExcelProperty(value = "事务发生日期", index = 3)
    private Date affairDate;
    @ExcelProperty(value = "关联部门ID（关联sys_dept.id）", index = 4)
    private Long deptId;
    @ExcelProperty(value = "租户编码", index = 5)
    private Long tenantCode;
    @ExcelProperty(value = "状态 0：停用 1：正常", index = 6)
    private Integer status;
    @ExcelProperty(value = "从原系统同步的时间（无则填NULL）", index = 7)
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
