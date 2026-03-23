package com.jslh.rjd.controller;

import com.jslh.commons.log.annotation.LogOperation;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.utils.ExcelUtils;
import com.jslh.commons.tools.validator.AssertUtils;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.commons.tools.validator.group.UpdateGroup;
import com.jslh.rjd.dto.RjdEmployeeCommuteRecordDTO;
import com.jslh.rjd.excel.RjdEmployeeCommuteRecordExcel;
import com.jslh.rjd.service.RjdEmployeeCommuteRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
* 管理人员通勤记录表（工时统计专用）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@RestController
@RequestMapping("rjdemployeecommuterecord")
@Tag(name = "管理人员通勤记录表（工时统计专用）")
public class RjdEmployeeCommuteRecordController {
    @Resource
    private RjdEmployeeCommuteRecordService rjdEmployeeCommuteRecordService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:rjdemployeecommuterecord:page')")
    public Result<PageData<RjdEmployeeCommuteRecordDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<RjdEmployeeCommuteRecordDTO> page = rjdEmployeeCommuteRecordService.page(params);

        return new Result<PageData<RjdEmployeeCommuteRecordDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:rjdemployeecommuterecord:info')")
    public Result<RjdEmployeeCommuteRecordDTO> get(@PathVariable("id") Long id){
        RjdEmployeeCommuteRecordDTO data = rjdEmployeeCommuteRecordService.get(id);

        return new Result<RjdEmployeeCommuteRecordDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:rjdemployeecommuterecord:save')")
    public Result save(@RequestBody RjdEmployeeCommuteRecordDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        rjdEmployeeCommuteRecordService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:rjdemployeecommuterecord:update')")
    public Result update(@RequestBody RjdEmployeeCommuteRecordDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        rjdEmployeeCommuteRecordService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:rjdemployeecommuterecord:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        rjdEmployeeCommuteRecordService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:rjdemployeecommuterecord:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<RjdEmployeeCommuteRecordDTO> list = rjdEmployeeCommuteRecordService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "管理人员通勤记录表（工时统计专用）", list, RjdEmployeeCommuteRecordExcel.class);
    }

}
