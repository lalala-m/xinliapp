/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.controller;

import com.jslh.commons.log.annotation.LogOperation;
import com.jslh.commons.tools.exception.ErrorCode;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.AssertUtils;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.commons.tools.validator.group.UpdateGroup;
import com.jslh.dto.SysRegionDTO;
import com.jslh.dto.region.RegionProvince;
import com.jslh.service.SysRegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 行政区域
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("region")
@Tag(name = "行政区域")
public class SysRegionController {
    @Resource
    private SysRegionService sysRegionService;

    @GetMapping("list")
    @Operation(summary = "列表")
    @Parameter(name = "pid", description = "上级ID")
    @PreAuthorize("hasAuthority('sys:region:list')")
    public Result<List<SysRegionDTO>> list(@RequestParam Map<String, Object> params) {
        List<SysRegionDTO> list = sysRegionService.list(params);

        return new Result<List<SysRegionDTO>>().ok(list);
    }

    @GetMapping("tree")
    @Operation(summary = "树形数据")
    public Result<List<Map<String, Object>>> tree() {
        List<Map<String, Object>> list = sysRegionService.getTreeList();

        return new Result<List<Map<String, Object>>>().ok(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:region:info')")
    public Result<SysRegionDTO> get(@PathVariable("id") Long id) {
        SysRegionDTO data = sysRegionService.get(id);

        return new Result<SysRegionDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:region:save')")
    public Result save(@RequestBody SysRegionDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        sysRegionService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:region:update')")
    public Result update(@RequestBody SysRegionDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        sysRegionService.update(dto);

        return new Result();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:region:delete')")
    public Result delete(@PathVariable("id") Long id) {
        //效验数据
        AssertUtils.isNull(id, "id");

        int count = sysRegionService.getCountByPid(id);
        if (count > 0) {
            throw new RenException(ErrorCode.REGION_SUB_DELETE_ERROR);
        }

        sysRegionService.delete(id);

        return new Result();
    }

    @GetMapping("region")
    @Operation(summary = "地区列表")
    @Parameter(name = "threeLevel", description = "是否显示3级   true显示   false不显示")
    public Result<List<RegionProvince>> region(@RequestParam(value = "threeLevel", defaultValue = "true") boolean threeLevel) {
        List<RegionProvince> list = sysRegionService.getRegion(threeLevel);

        return new Result<List<RegionProvince>>().ok(list);
    }

}
