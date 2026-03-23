package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 日结单系统审核配置表（精细化规则）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "日结单系统审核配置表（精细化规则）")
public class RjdSystemAuditConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    private Long configId;
    @Schema(description = "配置名称（如：生产管理人员日结单审核规则）")
    private String configName;
    @Schema(description = "适用岗位ID（sys_post.id，NULL=全岗位）")
    private Long postId;
    @Schema(description = "适用岗位名称")
    private String postName;
    @Schema(description = "适用部门ID（sys_dept.id，NULL=全部门）")
    private Long deptId;
    @Schema(description = "适用部门名称")
    private String deptName;
    @Schema(description = "必填字段（逗号分隔）")
    private String requiredFields;
    @Schema(description = "填报耗时最小值（小时）")
    private BigDecimal workingHoursMin;
    @Schema(description = "填报耗时最大值（小时）")
    private BigDecimal workingHoursMax;
    @Schema(description = "关联对象校验：0=不校验 1=校验")
    private Integer relatedObjectCheck;
    @Schema(description = "工时匹配：0=不校验 1=填报≤通勤")
    private Integer actualHoursMatch;
    @Schema(description = "自动通过：0=否 1=是")
    private Integer autoPass;
    @Schema(description = "填报内容最小字数（0=不限制）")
    private Integer fillContentMinLength;
    @Schema(description = "分摊校验：0=不校验 1=总和=总耗时")
    private Integer allocatedCheck;
    @Schema(description = "驳回后最大修改次数（0=不限制）")
    private Integer modifyLimit;
    @Schema(description = "关联对象最少数量（0=不限制）")
    private Integer relatedObjectMinCount;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态：0=停用 1=启用")
    private Integer status;
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
