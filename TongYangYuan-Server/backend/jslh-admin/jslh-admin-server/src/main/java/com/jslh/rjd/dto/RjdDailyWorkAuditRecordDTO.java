package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 精铸行业岗位日结单审核记录表（含分摊校验）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "精铸行业岗位日结单审核记录表（含分摊校验）")
public class RjdDailyWorkAuditRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "审核记录ID")
    private Long id;
    @Schema(description = "关联日结单ID")
    private Long dailyRecordId;
    @Schema(description = "审核节点：system=系统 manual=人工")
    private String auditNode;
    @Schema(description = "审核顺序：1=系统 2=人工")
    private Integer auditOrder;
    @Schema(description = "关联系统审核配置ID（仅system节点）")
    private Long systemConfigId;
    @Schema(description = "系统审核规则详情")
    private String auditRuleDetail;
    @Schema(description = "分摊校验详情")
    private String allocatedAuditDetail;
    @Schema(description = "审核类型：1=系统 2=人工")
    private Integer auditType;
    @Schema(description = "审核状态：0=待审核 1=通过 2=驳回")
    private Integer auditStatus;
    @Schema(description = "审核人ID（system=0，manual=sys_user.id）")
    private Long auditorId;
    @Schema(description = "审核人姓名（system=系统）")
    private String auditorName;
    @Schema(description = "审核完成时间")
    private Date auditTime;
    @Schema(description = "审核备注")
    private String auditRemark;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "创建者（system=0）")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "删除标识：0=未删 1=已删")
    private Integer delFlag;

}
