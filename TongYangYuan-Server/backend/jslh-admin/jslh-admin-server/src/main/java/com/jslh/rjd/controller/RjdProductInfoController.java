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
import com.jslh.rjd.dto.RjdProductInfoDTO;
import com.jslh.rjd.excel.RjdProductInfoExcel;
import com.jslh.rjd.service.RjdProductInfoService;
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
* 产品基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@RestController
@RequestMapping("rjdproductinfo")
@Tag(name = "产品基础表（同步原系统）")
public class RjdProductInfoController {
    @Resource
    private RjdProductInfoService rjdProductInfoService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:rjdproductinfo:page')")
    public Result<PageData<RjdProductInfoDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<RjdProductInfoDTO> page = rjdProductInfoService.page(params);

        return new Result<PageData<RjdProductInfoDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:rjdproductinfo:info')")
    public Result<RjdProductInfoDTO> get(@PathVariable("id") Long id){
        RjdProductInfoDTO data = rjdProductInfoService.get(id);

        return new Result<RjdProductInfoDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:rjdproductinfo:save')")
    public Result save(@RequestBody RjdProductInfoDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        rjdProductInfoService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:rjdproductinfo:update')")
    public Result update(@RequestBody RjdProductInfoDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        rjdProductInfoService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:rjdproductinfo:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        rjdProductInfoService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:rjdproductinfo:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<RjdProductInfoDTO> list = rjdProductInfoService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "产品基础表（同步原系统）", list, RjdProductInfoExcel.class);
    }

}
