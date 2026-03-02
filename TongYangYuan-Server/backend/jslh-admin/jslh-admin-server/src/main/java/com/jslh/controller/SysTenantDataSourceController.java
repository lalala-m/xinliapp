package com.jslh.controller;

import com.jslh.commons.log.annotation.LogOperation;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.AssertUtils;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.service.SysTenantDataSourceService;
import com.jslh.tenant.dto.SysTenantDataSourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("tenant/datasource")
@Tag(name = "租户数据源")
public class SysTenantDataSourceController {
    @Resource
    private SysTenantDataSourceService sysTenantDataSourceService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "tenantName", description = "租户名")
    })
    @PreAuthorize("hasAuthority('tenant:datasource:all')")
    public Result<PageData<SysTenantDataSourceDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysTenantDataSourceDTO> page = sysTenantDataSourceService.page(params);

        return new Result<PageData<SysTenantDataSourceDTO>>().ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('tenant:datasource:all')")
    public Result<List<SysTenantDataSourceDTO>> list() {
        List<SysTenantDataSourceDTO> data = sysTenantDataSourceService.list(new HashMap<>(1));

        return new Result<List<SysTenantDataSourceDTO>>().ok(data);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('tenant:datasource:all')")
    public Result<SysTenantDataSourceDTO> get(@PathVariable("id") Long id) {
        SysTenantDataSourceDTO data = sysTenantDataSourceService.get(id);

        return new Result<SysTenantDataSourceDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('tenant:datasource:all')")
    public Result save(@RequestBody SysTenantDataSourceDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto);

        sysTenantDataSourceService.save(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('tenant:datasource:all')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysTenantDataSourceService.delete(ids);

        return new Result();
    }

}
