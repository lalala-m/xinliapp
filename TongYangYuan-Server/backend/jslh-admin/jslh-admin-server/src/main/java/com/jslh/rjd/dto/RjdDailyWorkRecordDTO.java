package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 精铸行业岗位日结单主表（含耗时分摊校验）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "精铸行业岗位日结单主表（含耗时分摊校验）")
public class RjdDailyWorkRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "岗位ID（关联sys_post.id）")
    private Long postId;
    @Schema(description = "岗位名称（冗余存储）")
    private String postName;
    @Schema(description = "所属部门ID（关联sys_dept.id）")
    private Long deptId;
    @Schema(description = "所属部门名称（冗余存储）")
    private String deptName;
    @Schema(description = "填报日期")
    private Date workDate;
    @Schema(description = "工作事项分类（关联rjd_post_work_item.work_item_name）")
    private String workItemType;
    @Schema(description = "关联事项类型ID（关联rjd_related_object_type_dict.type_id）")
    private Integer relatedTypeId;
    @Schema(description = "关联事项类型名称")
    private String relatedTypeName;
    @Schema(description = "关联事项来源表")
    private String relatedSourceTable;
    @Schema(description = "关联事项名称")
    private String relatedObjectName;
    @Schema(description = "填报关联对象")
    private String relatedObject;
    @Schema(description = "填报内容")
    private String fillContent;
    @Schema(description = "总耗时（小时）")
    private BigDecimal workingHours;
    @Schema(description = "关联对象分摊耗时总和（系统自动计算）")
    private BigDecimal allocatedHoursSum;
    @Schema(description = "分摊校验状态：0=未校验 1=通过 2=失败")
    private Integer allocatedCheckStatus;
    @Schema(description = "驳回后修改次数")
    private Integer modifyCount;
    @Schema(description = "最新驳回原因")
    private String latestRejectReason;
    @Schema(description = "通勤实际工时（冗余自rjd_employee_commute_record）")
    private BigDecimal commuteActualHours;
    @Schema(description = "系统审核状态：0=待审核 1=通过 2=驳回")
    private Integer systemAuditStatus;
    @Schema(description = "人工审核状态：0=待审核 1=通过 2=驳回")
    private Integer manualAuditStatus;
    @Schema(description = "当前审核节点：system=系统 manual=人工")
    private String latestAuditNode;
    @Schema(description = "填报人ID（关联sys_user.id）")
    private Long fillerId;
    @Schema(description = "填报人姓名（冗余存储）")
    private String fillerName;
    @Schema(description = "填报时间")
    private Date fillDatetime;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态：0=停用 1=正常")
    private Integer status;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;
    @Schema(description = "删除标识：0=未删 1=已删")
    private Integer delFlag;

}
