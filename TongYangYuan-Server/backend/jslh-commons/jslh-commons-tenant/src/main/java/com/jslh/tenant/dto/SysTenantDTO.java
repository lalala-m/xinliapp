/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jslh.commons.tools.utils.DateUtils;
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.commons.tools.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "租户管理")
public class SysTenantDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "租户数据源")
    private Long datasourceId;

    @Schema(description = "域名")
    private String tenantDomain;

    @Schema(description = "租户名称")
    @NotBlank(message = "{tenant.tenantName.require}", groups = DefaultGroup.class)
    private String tenantName;

    @Schema(description = "登录账号")
    @NotBlank(message = "{tenant.username.require}", groups = DefaultGroup.class)
    private String username;

    @Schema(description = "登录密码")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "{tenant.password.require}", groups = AddGroup.class)
    private String password;

    @Schema(description = "备注")
    private String remark;

    @JsonIgnore
    private Long userId;

    @Schema(description = "租户模式")
    @NotNull(message = "租户模式不能为空", groups = DefaultGroup.class)
    private Integer tenantMode;

    @Schema(description = "状态  0：停用    1：正常", required = true)
    @Range(min = 0, max = 1, message = "{tenant.status.range}", groups = DefaultGroup.class)
    private Integer status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIdList;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}
