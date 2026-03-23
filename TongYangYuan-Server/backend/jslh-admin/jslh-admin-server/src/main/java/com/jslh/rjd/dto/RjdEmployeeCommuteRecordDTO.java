package com.jslh.rjd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jslh.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 管理人员通勤记录表（工时统计专用）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@Data
@Schema(description = "管理人员通勤记录表（工时统计专用）")
public class RjdEmployeeCommuteRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "员工ID（关联sys_user.id）")
    private Long empId;
    @Schema(description = "员工姓名（冗余存储）")
    private String empName;
    @Schema(description = "所属部门ID（关联sys_dept.id）")
    private Long deptId;
    @Schema(description = "所属部门名称（冗余存储）")
    private String deptName;
    @Schema(description = "岗位ID（关联sys_post.id）")
    private Long postId;
    @Schema(description = "岗位名称（筛选管理人员）")
    private String postName;
    @Schema(description = "统计日期（与日结单一致）")
    private Date workDate;
    @Schema(description = "上班打卡时间（考勤同步）")
    private Date checkInTime;
    @Schema(description = "下班打卡时间（考勤同步）")
    private Date checkOutTime;
    @Schema(description = "出勤状态：1=正常 2=迟到 3=早退 4=旷工 5=事假 6=病假 7=其他")
    private Integer attendanceStatus;
    @Schema(description = "午休扣除时长（小时）")
    private BigDecimal lunchBreakHours;
    @Schema(description = "标准工时（小时）")
    private BigDecimal scheduledWorkingHours;
    @Schema(description = "实际工时（自动计算）")
    private BigDecimal actualWorkingHours;
    @Schema(description = "迟到分钟数（自动计算）")
    private Integer lateMinutes;
    @Schema(description = "早退分钟数（自动计算）")
    private Integer earlyLeaveMinutes;
    @Schema(description = "备注")
    private String commuteRemark;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "考勤同步状态：0=未同步 1=成功 2=失败")
    private Integer syncStatus;
    @Schema(description = "考勤同步时间")
    private Date syncTime;
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
