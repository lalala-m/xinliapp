package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 精铸行业岗位日结单主表（含耗时分摊校验）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdDailyWorkRecordExcel {
    @ExcelProperty(value = "主键ID", index = 0)
    private Long id;
    @ExcelProperty(value = "岗位ID（关联sys_post.id）", index = 1)
    private Long postId;
    @ExcelProperty(value = "岗位名称（冗余存储）", index = 2)
    private String postName;
    @ExcelProperty(value = "所属部门ID（关联sys_dept.id）", index = 3)
    private Long deptId;
    @ExcelProperty(value = "所属部门名称（冗余存储）", index = 4)
    private String deptName;
    @ExcelProperty(value = "填报日期", index = 5)
    private Date workDate;
    @ExcelProperty(value = "工作事项分类（关联rjd_post_work_item.work_item_name）", index = 6)
    private String workItemType;
    @ExcelProperty(value = "关联事项类型ID（关联rjd_related_object_type_dict.type_id）", index = 7)
    private Integer relatedTypeId;
    @ExcelProperty(value = "关联事项类型名称", index = 8)
    private String relatedTypeName;
    @ExcelProperty(value = "关联事项来源表", index = 9)
    private String relatedSourceTable;
    @ExcelProperty(value = "关联事项名称", index = 10)
    private String relatedObjectName;
    @ExcelProperty(value = "填报关联对象", index = 11)
    private String relatedObject;
    @ExcelProperty(value = "填报内容", index = 12)
    private String fillContent;
    @ExcelProperty(value = "总耗时（小时）", index = 13)
    private BigDecimal workingHours;
    @ExcelProperty(value = "关联对象分摊耗时总和（系统自动计算）", index = 14)
    private BigDecimal allocatedHoursSum;
    @ExcelProperty(value = "分摊校验状态：0=未校验 1=通过 2=失败", index = 15)
    private Integer allocatedCheckStatus;
    @ExcelProperty(value = "驳回后修改次数", index = 16)
    private Integer modifyCount;
    @ExcelProperty(value = "最新驳回原因", index = 17)
    private String latestRejectReason;
    @ExcelProperty(value = "通勤实际工时（冗余自rjd_employee_commute_record）", index = 18)
    private BigDecimal commuteActualHours;
    @ExcelProperty(value = "系统审核状态：0=待审核 1=通过 2=驳回", index = 19)
    private Integer systemAuditStatus;
    @ExcelProperty(value = "人工审核状态：0=待审核 1=通过 2=驳回", index = 20)
    private Integer manualAuditStatus;
    @ExcelProperty(value = "当前审核节点：system=系统 manual=人工", index = 21)
    private String latestAuditNode;
    @ExcelProperty(value = "填报人ID（关联sys_user.id）", index = 22)
    private Long fillerId;
    @ExcelProperty(value = "填报人姓名（冗余存储）", index = 23)
    private String fillerName;
    @ExcelProperty(value = "填报时间", index = 24)
    private Date fillDatetime;
    @ExcelProperty(value = "租户编码", index = 25)
    private Long tenantCode;
    @ExcelProperty(value = "状态：0=停用 1=正常", index = 26)
    private Integer status;
    @ExcelProperty(value = "创建者", index = 27)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 28)
    private Date createDate;
    @ExcelProperty(value = "更新者", index = 29)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 30)
    private Date updateDate;
    @ExcelProperty(value = "删除标识：0=未删 1=已删", index = 31)
    private Integer delFlag;
}
