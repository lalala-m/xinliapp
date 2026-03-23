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
import com.jslh.rjd.dto.RjdPostWorkItemDTO;
import com.jslh.rjd.excel.RjdPostWorkItemExcel;
import com.jslh.rjd.service.RjdPostWorkItemService;
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
* 精铸行业岗位工作事项字典表
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@RestController
@RequestMapping("rjdpostworkitem")
@Tag(name = "精铸行业岗位工作事项字典表")
public class RjdPostWorkItemController {
    @Resource
    private RjdPostWorkItemService rjdPostWorkItemService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:rjdpostworkitem:page')")
    public Result<PageData<RjdPostWorkItemDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<RjdPostWorkItemDTO> page = rjdPostWorkItemService.page(params);

        return new Result<PageData<RjdPostWorkItemDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:rjdpostworkitem:info')")
    public Result<RjdPostWorkItemDTO> get(@PathVariable("id") Long id){
        RjdPostWorkItemDTO data = rjdPostWorkItemService.get(id);

        return new Result<RjdPostWorkItemDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:rjdpostworkitem:save')")
    public Result save(@RequestBody RjdPostWorkItemDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        rjdPostWorkItemService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:rjdpostworkitem:update')")
    public Result update(@RequestBody RjdPostWorkItemDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        rjdPostWorkItemService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:rjdpostworkitem:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        rjdPostWorkItemService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:rjdpostworkitem:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<RjdPostWorkItemDTO> list = rjdPostWorkItemService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "精铸行业岗位工作事项字典表", list, RjdPostWorkItemExcel.class);
    }

}
