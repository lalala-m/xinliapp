package com.jslh.rjd.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.math.BigDecimal;
	import java.util.Date;
	import com.jslh.commons.mybatis.entity.BaseEntity;

/**
* 管理人员通勤记录表（工时统计专用）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("rjd_employee_commute_record")
public class RjdEmployeeCommuteRecordEntity extends BaseEntity {
private static final long serialVersionUID = 1L;

			/**
			* 员工ID（关联sys_user.id）
			*/
		private Long empId;
			/**
			* 员工姓名（冗余存储）
			*/
		private String empName;
			/**
			* 所属部门ID（关联sys_dept.id）
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long deptId;
			/**
			* 所属部门名称（冗余存储）
			*/
		private String deptName;
			/**
			* 岗位ID（关联sys_post.id）
			*/
		private Long postId;
			/**
			* 岗位名称（筛选管理人员）
			*/
		private String postName;
			/**
			* 统计日期（与日结单一致）
			*/
		private Date workDate;
			/**
			* 上班打卡时间（考勤同步）
			*/
		private Date checkInTime;
			/**
			* 下班打卡时间（考勤同步）
			*/
		private Date checkOutTime;
			/**
			* 出勤状态：1=正常 2=迟到 3=早退 4=旷工 5=事假 6=病假 7=其他
			*/
		private Integer attendanceStatus;
			/**
			* 午休扣除时长（小时）
			*/
		private BigDecimal lunchBreakHours;
			/**
			* 标准工时（小时）
			*/
		private BigDecimal scheduledWorkingHours;
			/**
			* 实际工时（自动计算）
			*/
		private BigDecimal actualWorkingHours;
			/**
			* 迟到分钟数（自动计算）
			*/
		private Integer lateMinutes;
			/**
			* 早退分钟数（自动计算）
			*/
		private Integer earlyLeaveMinutes;
			/**
			* 备注
			*/
		private String commuteRemark;
			/**
			* 租户编码
			*/
		private Long tenantCode;
			/**
			* 考勤同步状态：0=未同步 1=成功 2=失败
			*/
		private Integer syncStatus;
			/**
			* 考勤同步时间
			*/
		private Date syncTime;
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
			* 删除标识：0=未删 1=已删
			*/
		private Integer delFlag;
}
