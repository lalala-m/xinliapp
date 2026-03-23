package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 精铸行业岗位日结单审核记录表（含分摊校验）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdDailyWorkAuditRecordExcel {
    @ExcelProperty(value = "审核记录ID", index = 0)
    private Long id;
    @ExcelProperty(value = "关联日结单ID", index = 1)
    private Long dailyRecordId;
    @ExcelProperty(value = "审核节点：system=系统 manual=人工", index = 2)
    private String auditNode;
    @ExcelProperty(value = "审核顺序：1=系统 2=人工", index = 3)
    private Integer auditOrder;
    @ExcelProperty(value = "关联系统审核配置ID（仅system节点）", index = 4)
    private Long systemConfigId;
    @ExcelProperty(value = "系统审核规则详情", index = 5)
    private String auditRuleDetail;
    @ExcelProperty(value = "分摊校验详情", index = 6)
    private String allocatedAuditDetail;
    @ExcelProperty(value = "审核类型：1=系统 2=人工", index = 7)
    private Integer auditType;
    @ExcelProperty(value = "审核状态：0=待审核 1=通过 2=驳回", index = 8)
    private Integer auditStatus;
    @ExcelProperty(value = "审核人ID（system=0，manual=sys_user.id）", index = 9)
    private Long auditorId;
    @ExcelProperty(value = "审核人姓名（system=系统）", index = 10)
    private String auditorName;
    @ExcelProperty(value = "审核完成时间", index = 11)
    private Date auditTime;
    @ExcelProperty(value = "审核备注", index = 12)
    private String auditRemark;
    @ExcelProperty(value = "租户编码", index = 13)
    private Long tenantCode;
    @ExcelProperty(value = "创建者（system=0）", index = 14)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 15)
    private Date createDate;
    @ExcelProperty(value = "删除标识：0=未删 1=已删", index = 16)
    private Integer delFlag;
}
