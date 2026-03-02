package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 关联对象类型字典表
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "关联对象类型字典表")
public class RjdRelatedObjectTypeDictDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联对象类型ID")
    private Integer typeId;
    @Schema(description = "关联对象类型名称")
    private String typeName;
    @Schema(description = "类型描述")
    private String typeDesc;
    @Schema(description = "对应基础数据表名")
    private String sourceTable;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态 0：停用 1：正常")
    private Integer status;
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
