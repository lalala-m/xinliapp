package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 日结单-关联对象中间表（含耗时分摊）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "日结单-关联对象中间表（含耗时分摊）")
public class RjdDailyWorkRelatedObjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "关联日结单ID（rjd_daily_work_record.id）")
    private Long dailyRecordId;
    @Schema(description = "关联对象类型ID（rjd_related_object_type_dict.type_id）")
    private Integer relatedTypeId;
    @Schema(description = "关联对象ID（产品/客户/订单/事务ID）")
    private Long relatedObjectId;
    @Schema(description = "关联对象名称（冗余存储）")
    private String relatedObjectName;
    @Schema(description = "分摊到该对象的耗时（小时）")
    private BigDecimal allocatedHours;
    @Schema(description = "分摊比例（%）")
    private BigDecimal allocatedRatio;
    @Schema(description = "分摊备注")
    private String allocatedRemark;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "创建者（关联sys_user.id）")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者（关联sys_user.id）")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;
    @Schema(description = "删除标识：0=未删 1=已删")
    private Integer delFlag;

}
