/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.form.controller;

import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.commons.tools.validator.group.AddGroup;
import com.jslh.commons.tools.validator.group.DefaultGroup;
import com.jslh.form.dto.CorrectionDTO;
import com.jslh.form.service.CorrectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 转正申请
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("form/correction")
@AllArgsConstructor
@Tag(name = "转正申请")
public class CorrectionController {
    private final CorrectionService correctionService;


    @PostMapping("start")
    @Operation(summary = "启动流程")
    public Result startProcess(@RequestBody CorrectionDTO dto) {
        // 效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);


        correctionService.save(dto);

        return new Result();
    }

    @GetMapping("{instanceId}")
    @Operation(summary = "表单信息")
    public Result<CorrectionDTO> info(@PathVariable("instanceId") String instanceId) {
        CorrectionDTO correction = correctionService.get(instanceId);

        return new Result<CorrectionDTO>().ok(correction);
    }

}
