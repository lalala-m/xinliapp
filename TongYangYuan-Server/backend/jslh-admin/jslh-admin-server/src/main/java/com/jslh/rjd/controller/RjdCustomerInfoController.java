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
import com.jslh.rjd.dto.RjdCustomerInfoDTO;
import com.jslh.rjd.excel.RjdCustomerInfoExcel;
import com.jslh.rjd.service.RjdCustomerInfoService;
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
* 客户基础表（同步原系统）
*
* @author LHC lhc@gmail.com
* @since 3.0 2025-12-11
*/
@RestController
@RequestMapping("rjdcustomerinfo")
@Tag(name = "客户基础表（同步原系统）")
public class RjdCustomerInfoController {
    @Resource
    private RjdCustomerInfoService rjdCustomerInfoService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:rjdcustomerinfo:page')")
    public Result<PageData<RjdCustomerInfoDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<RjdCustomerInfoDTO> page = rjdCustomerInfoService.page(params);

        return new Result<PageData<RjdCustomerInfoDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:rjdcustomerinfo:info')")
    public Result<RjdCustomerInfoDTO> get(@PathVariable("id") Long id){
        RjdCustomerInfoDTO data = rjdCustomerInfoService.get(id);

        return new Result<RjdCustomerInfoDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:rjdcustomerinfo:save')")
    public Result save(@RequestBody RjdCustomerInfoDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        rjdCustomerInfoService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:rjdcustomerinfo:update')")
    public Result update(@RequestBody RjdCustomerInfoDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        rjdCustomerInfoService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:rjdcustomerinfo:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        rjdCustomerInfoService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:rjdcustomerinfo:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<RjdCustomerInfoDTO> list = rjdCustomerInfoService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "客户基础表（同步原系统）", list, RjdCustomerInfoExcel.class);
    }

}
