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
import com.jslh.dto.SysMailLogDTO;
import com.jslh.service.SysMailLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 邮件发送记录
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("maillog")
@Tag(name = "邮件发送记录")
public class MailLogController {
    @Resource
    private SysMailLogService sysMailLogService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "templateId", description = "templateId"),
            @Parameter(name = "mailTo", description = "mailTo"),
            @Parameter(name = "status", description = "status")
    })
    @PreAuthorize("hasAuthority('sys:mail:log')")
    public Result<PageData<SysMailLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysMailLogDTO> page = sysMailLogService.page(params);

        return new Result<PageData<SysMailLogDTO>>().ok(page);
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:mail:log')")
    public Result delete(@RequestBody Long[] ids) {
        sysMailLogService.deleteBatchIds(Arrays.asList(ids));

        return new Result();
    }

}
