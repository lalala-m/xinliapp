package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.util.Date;

/**
* 精铸行业岗位工作事项字典表
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_post_work_item")
public class RjdPostWorkItemEntity {
private static final long serialVersionUID = 1L;

			/**
			* 事项ID
			*/
			@TableId
		private Long itemId;
			/**
			* 岗位ID（关联sys_post.id）
			*/
		private Long postId;
			/**
			* 岗位名称（冗余存储）
			*/
		private String postName;
			/**
			* 工作事项名称
			*/
		private String workItemName;
			/**
			* 排序号
			*/
		private Long sort;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 状态  0：停用   1：正常
			*/
		private Integer status;
			/**
			* 创建者（关联sys_user.id）
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long creator;
			/**
			* 创建时间
			*/
			@TableField(fill = FieldFill.INSERT)
		private Date createDate;
			/**
			* 更新者（关联sys_user.id）
			*/
			@TableField(fill = FieldFill.INSERT_UPDATE)
		private Long updater;
			/**
			* 更新时间
			*/
			@TableField(fill = FieldFill.INSERT_UPDATE)
		private Date updateDate;
			/**
			* 删除标识  0：未删除    1：删除
			*/
		private Integer delFlag;
}
