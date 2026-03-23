package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 产品基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "产品基础表（同步原系统）")
public class RjdProductInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "产品ID（与原系统一致）")
    private Long productId;
    @Schema(description = "产品型号（核心标识）")
    private String productModel;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "产品规格")
    private String productSpec;
    @Schema(description = "产品类型（如精密铸件、模具等）")
    private String productType;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态 0：停用 1：正常")
    private Integer status;
    @Schema(description = "从原系统同步的时间")
    private Date syncTime;
    @Schema(description = "创建者（关联sys_user.id）")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者（关联sys_user.id）")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;
    @Schema(description = "删除标识  0：未删除    1：删除")
    private Integer delFlag;

}
