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
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.commons.tools.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮件模板
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "邮件模板")
public class SysMailTemplateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @Null(message = "{id.null}", groups = AddGroup.class)
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "模板名称")
    @NotBlank(message = "{mail.name.require}", groups = DefaultGroup.class)
    private String name;

    @Schema(description = "邮件主题")
    @NotBlank(message = "{mail.subject.require}", groups = DefaultGroup.class)
    private String subject;

    @Schema(description = "邮件正文")
    @NotBlank(message = "{mail.content.require}", groups = DefaultGroup.class)
    private String content;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}
