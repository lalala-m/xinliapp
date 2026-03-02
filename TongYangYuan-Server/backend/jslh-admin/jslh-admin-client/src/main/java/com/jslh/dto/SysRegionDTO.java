/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jslh.commons.tools.utils.DateUtils;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 行政区域
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "行政区域")
public class SysRegionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "区域标识")
    @NotNull(message = "{id.require}", groups = DefaultGroup.class)
    private Long id;

    @Schema(description = "上级区域ID")
    @NotNull(message = "{region.pid.require}", groups = DefaultGroup.class)
    private Long pid;

    @Schema(description = "区域名称")
    @NotBlank(message = "{region.name.require}", groups = DefaultGroup.class)
    private String name;

    @Schema(description = "排序")
    @Min(value = 0, message = "{sort.number}", groups = DefaultGroup.class)
    private Long sort;

    @Schema(description = "上级区域名称")
    private String parentName;

    @Schema(description = "是否有子节点")
    private Boolean hasChildren;

    @Schema(description = "层级")
    private Integer treeLevel;

    @Schema(description = "更新时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date updateDate;
}
