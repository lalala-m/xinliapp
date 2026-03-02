/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.controller;

import com.jslh.commons.log.annotation.LogOperation;
import com.jslh.commons.security.context.TenantContext;
import com.jslh.commons.security.user.SecurityUser;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.enums.TenantModeEnum;
import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.AssertUtils;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.commons.tools.validator.group.UpdateGroup;
import com.jslh.service.SysRoleUserService;
import com.jslh.service.SysTenantService;
import com.jslh.tenant.dto.SysTenantDTO;
import com.jslh.tenant.dto.SysTenantListDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("tenant")
@Tag(name = "租户管理")
public class SysTenantController {
    @Resource
    private SysTenantService sysTenantService;
    @Resource
    private SysRoleUserService sysRoleUserService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "tenantName", description = "租户名")
    })
    @PreAuthorize("hasAuthority('sys:tenant:all')")
    public Result<PageData<SysTenantDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysTenantDTO> page = sysTenantService.page(params);

        return new Result<PageData<SysTenantDTO>>().ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    public Result<List<SysTenantListDTO>> list() {
        List<SysTenantListDTO> list = sysTenantService.list();

        return new Result<List<SysTenantListDTO>>().ok(list);
    }

    @GetMapping("info")
    @Operation(summary = "当前租户信息")
    public Result<SysTenantDTO> info() {
        // 获取当前租户ID
        Long tenantId = TenantContext.getTenantCode(SecurityUser.getUser());
        SysTenantDTO data = sysTenantService.get(tenantId);

        return new Result<SysTenantDTO>().ok(data);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:tenant:all')")
    public Result<SysTenantDTO> get(@PathVariable("id") Long id) {
        SysTenantDTO data = sysTenantService.get(id);

        // 字段隔离，则需要查询对应的角色
        if (data.getTenantMode() == TenantModeEnum.COLUMN.value()) {
            // 用户角色列表
            List<Long> roleIdList = sysRoleUserService.getRoleIdList(data.getUserId());
            data.setRoleIdList(roleIdList);
        }

        return new Result<SysTenantDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:tenant:all')")
    public Result save(@RequestBody SysTenantDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        sysTenantService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:tenant:all')")
    public Result update(@RequestBody SysTenantDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        sysTenantService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:tenant:all')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysTenantService.deleteBatchIds(Arrays.asList(ids));

        return new Result();
    }
}
