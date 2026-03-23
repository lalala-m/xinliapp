package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 精铸行业岗位工作事项字典表
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "精铸行业岗位工作事项字典表")
public class RjdPostWorkItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "事项ID")
    private Long itemId;
    @Schema(description = "岗位ID（关联sys_post.id）")
    private Long postId;
    @Schema(description = "岗位名称（冗余存储）")
    private String postName;
    @Schema(description = "工作事项名称")
    private String workItemName;
    @Schema(description = "排序号")
    private Long sort;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "状态  0：停用   1：正常")
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
