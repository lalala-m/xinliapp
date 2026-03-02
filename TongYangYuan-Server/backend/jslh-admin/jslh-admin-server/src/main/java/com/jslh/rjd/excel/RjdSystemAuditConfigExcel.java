package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 日结单系统审核配置表（精细化规则）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdSystemAuditConfigExcel {
    @ExcelProperty(value = "配置ID", index = 0)
    private Long configId;
    @ExcelProperty(value = "配置名称（如：生产管理人员日结单审核规则）", index = 1)
    private String configName;
    @ExcelProperty(value = "适用岗位ID（sys_post.id，NULL=全岗位）", index = 2)
    private Long postId;
    @ExcelProperty(value = "适用岗位名称", index = 3)
    private String postName;
    @ExcelProperty(value = "适用部门ID（sys_dept.id，NULL=全部门）", index = 4)
    private Long deptId;
    @ExcelProperty(value = "适用部门名称", index = 5)
    private String deptName;
    @ExcelProperty(value = "必填字段（逗号分隔）", index = 6)
    private String requiredFields;
    @ExcelProperty(value = "填报耗时最小值（小时）", index = 7)
    private BigDecimal workingHoursMin;
    @ExcelProperty(value = "填报耗时最大值（小时）", index = 8)
    private BigDecimal workingHoursMax;
    @ExcelProperty(value = "关联对象校验：0=不校验 1=校验", index = 9)
    private Integer relatedObjectCheck;
    @ExcelProperty(value = "工时匹配：0=不校验 1=填报≤通勤", index = 10)
    private Integer actualHoursMatch;
    @ExcelProperty(value = "自动通过：0=否 1=是", index = 11)
    private Integer autoPass;
    @ExcelProperty(value = "填报内容最小字数（0=不限制）", index = 12)
    private Integer fillContentMinLength;
    @ExcelProperty(value = "分摊校验：0=不校验 1=总和=总耗时", index = 13)
    private Integer allocatedCheck;
    @ExcelProperty(value = "驳回后最大修改次数（0=不限制）", index = 14)
    private Integer modifyLimit;
    @ExcelProperty(value = "关联对象最少数量（0=不限制）", index = 15)
    private Integer relatedObjectMinCount;
    @ExcelProperty(value = "租户编码", index = 16)
    private Long tenantCode;
    @ExcelProperty(value = "状态：0=停用 1=启用", index = 17)
    private Integer status;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 18)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 19)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 20)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 21)
    private Date updateDate;
    @ExcelProperty(value = "删除标识：0=未删 1=已删", index = 22)
    private Integer delFlag;
}
