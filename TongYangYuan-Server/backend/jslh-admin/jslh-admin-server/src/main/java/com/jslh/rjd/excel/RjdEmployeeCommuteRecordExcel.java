package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理人员通勤记录表（工时统计专用）
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdEmployeeCommuteRecordExcel {
    @ExcelProperty(value = "主键ID", index = 0)
    private Long id;
    @ExcelProperty(value = "员工ID（关联sys_user.id）", index = 1)
    private Long empId;
    @ExcelProperty(value = "员工姓名（冗余存储）", index = 2)
    private String empName;
    @ExcelProperty(value = "所属部门ID（关联sys_dept.id）", index = 3)
    private Long deptId;
    @ExcelProperty(value = "所属部门名称（冗余存储）", index = 4)
    private String deptName;
    @ExcelProperty(value = "岗位ID（关联sys_post.id）", index = 5)
    private Long postId;
    @ExcelProperty(value = "岗位名称（筛选管理人员）", index = 6)
    private String postName;
    @ExcelProperty(value = "统计日期（与日结单一致）", index = 7)
    private Date workDate;
    @ExcelProperty(value = "上班打卡时间（考勤同步）", index = 8)
    private Date checkInTime;
    @ExcelProperty(value = "下班打卡时间（考勤同步）", index = 9)
    private Date checkOutTime;
    @ExcelProperty(value = "出勤状态：1=正常 2=迟到 3=早退 4=旷工 5=事假 6=病假 7=其他", index = 10)
    private Integer attendanceStatus;
    @ExcelProperty(value = "午休扣除时长（小时）", index = 11)
    private BigDecimal lunchBreakHours;
    @ExcelProperty(value = "标准工时（小时）", index = 12)
    private BigDecimal scheduledWorkingHours;
    @ExcelProperty(value = "实际工时（自动计算）", index = 13)
    private BigDecimal actualWorkingHours;
    @ExcelProperty(value = "迟到分钟数（自动计算）", index = 14)
    private Integer lateMinutes;
    @ExcelProperty(value = "早退分钟数（自动计算）", index = 15)
    private Integer earlyLeaveMinutes;
    @ExcelProperty(value = "备注", index = 16)
    private String commuteRemark;
    @ExcelProperty(value = "租户编码", index = 17)
    private Long tenantCode;
    @ExcelProperty(value = "考勤同步状态：0=未同步 1=成功 2=失败", index = 18)
    private Integer syncStatus;
    @ExcelProperty(value = "考勤同步时间", index = 19)
    private Date syncTime;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 20)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 21)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 22)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 23)
    private Date updateDate;
    @ExcelProperty(value = "删除标识：0=未删 1=已删", index = 24)
    private Integer delFlag;
}
