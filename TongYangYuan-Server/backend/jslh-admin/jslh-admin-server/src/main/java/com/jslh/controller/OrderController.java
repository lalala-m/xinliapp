/**
 * Copyright (c) 2021 晶石领航 All rights reserved.
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
import com.jslh.dto.OrderDTO;
import com.jslh.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 订单
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("pay/order")
@Tag(name = "订单")
public class OrderController {
    @Resource
    private OrderService orderService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('pay:order:all')")
    public Result<PageData<OrderDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<OrderDTO> page = orderService.page(params);

        return new Result<PageData<OrderDTO>>().ok(page);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('pay:order:all')")
    public Result save(@RequestBody OrderDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        orderService.save(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('pay:order:all')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        orderService.delete(ids);

        return new Result();
    }

}
