/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.controller;

import com.jslh.commons.tools.utils.Result;
import com.jslh.service.FlowProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 流程管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("process")
@AllArgsConstructor
@Tag(name = "流程管理")
public class FlowProcessController {
    private final FlowProcessService flowProcessService;

    @PutMapping("active/{id}")
    @PreAuthorize("hasAuthority('sys:process:all')")
    public Result active(@PathVariable("id") String id) {
        flowProcessService.active(id);

        return new Result();
    }

    @PutMapping("suspend/{id}")
    @Operation(summary = "挂起流程")
    @PreAuthorize("hasAuthority('sys:process:all')")
    public Result suspend(@PathVariable("id") String id) {
        flowProcessService.suspend(id);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除流程")
    @PreAuthorize("hasAuthority('sys:process:all')")
    public Result delete(@RequestBody String[] deploymentIds) {
        for (String deploymentId : deploymentIds) {
            flowProcessService.deleteDeployment(deploymentId);
        }
        return new Result();
    }

}
