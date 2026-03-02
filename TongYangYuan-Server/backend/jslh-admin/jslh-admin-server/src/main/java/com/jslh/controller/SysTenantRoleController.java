/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.controller;

import com.jslh.commons.log.annotation.LogOperation;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.AssertUtils;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.commons.tools.validator.group.UpdateGroup;
import com.jslh.dto.SysRoleDTO;
import com.jslh.service.SysRoleMenuService;
import com.jslh.service.SysTenantRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户角色管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("tenant/role")
@Tag(name = "租户角色管理")
@AllArgsConstructor
public class SysTenantRoleController {
    private final SysTenantRoleService sysTenantRoleService;
    private final SysRoleMenuService sysRoleMenuService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "name", description = "角色名")
    })
    @PreAuthorize("hasAuthority('sys:tenantrole:all')")
    public Result<PageData<SysRoleDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysRoleDTO> page = sysTenantRoleService.page(params);

        return new Result<PageData<SysRoleDTO>>().ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    @PreAuthorize("hasAuthority('sys:tenantrole:all')")
    public Result<List<SysRoleDTO>> list() {
        List<SysRoleDTO> data = sysTenantRoleService.list(new HashMap<>(1));

        return new Result<List<SysRoleDTO>>().ok(data);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:tenantrole:all')")
    public Result<SysRoleDTO> get(@PathVariable("id") Long id) {
        SysRoleDTO data = sysTenantRoleService.get(id);

        //查询角色对应的菜单
        List<Long> menuIdList = sysRoleMenuService.getMenuIdList(id);
        data.setMenuIdList(menuIdList);

        return new Result<SysRoleDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:tenantrole:all')")
    public Result save(@RequestBody SysRoleDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        sysTenantRoleService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:tenantrole:all')")
    public Result update(@RequestBody SysRoleDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        sysTenantRoleService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:tenantrole:all')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysTenantRoleService.delete(ids);

        return new Result();
    }
}
