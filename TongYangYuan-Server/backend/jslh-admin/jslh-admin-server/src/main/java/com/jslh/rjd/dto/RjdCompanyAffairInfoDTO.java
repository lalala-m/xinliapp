package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 公司事务基础表（同步/录入）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "公司事务基础表（同步/录入）")
public class RjdCompanyAffairInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "事务ID（自增）")
    private Long affairId;
    @Schema(description = "事务名称（如成本内控会议、安全培训等）")
    private String affairName;
    @Schema(description = "事务类型（会议/培训/内控/其他）")
    private String affairType;
    @Schema(description = "事务发生日期")
    private Date affairDate;
    @Schema(description = "关联部门ID（关联sys_dept.id）")
    private Long deptId;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态 0：停用 1：正常")
    private Integer status;
    @Schema(description = "从原系统同步的时间（无则填NULL）")
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
