package com.jslh.rjd.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 精铸行业岗位工作事项字典表
 *
 * @author LHC lhc@gmail.com
 * @since 3.0 2025-12-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class RjdPostWorkItemExcel {
    @ExcelProperty(value = "事项ID", index = 0)
    private Long itemId;
    @ExcelProperty(value = "岗位ID（关联sys_post.id）", index = 1)
    private Long postId;
    @ExcelProperty(value = "岗位名称（冗余存储）", index = 2)
    private String postName;
    @ExcelProperty(value = "工作事项名称", index = 3)
    private String workItemName;
    @ExcelProperty(value = "排序号", index = 4)
    private Long sort;
    @ExcelProperty(value = "租户编码", index = 5)
    private Long tenantCode;
    @ExcelProperty(value = "状态  0：停用   1：正常", index = 6)
    private Integer status;
    @ExcelProperty(value = "创建者（关联sys_user.id）", index = 7)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 8)
    private Date createDate;
    @ExcelProperty(value = "更新者（关联sys_user.id）", index = 9)
    private Long updater;
    @ExcelProperty(value = "更新时间", index = 10)
    private Date updateDate;
    @ExcelProperty(value = "删除标识  0：未删除    1：删除", index = 11)
    private Integer delFlag;
}
