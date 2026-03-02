/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.controller;


import com.jslh.annotation.Login;
import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.dto.LoginDTO;
import com.jslh.service.TokenService;
import com.jslh.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录接口
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("auth")
@Tag(name = "登录接口")
public class ApiLoginController {
    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;


    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        //表单校验
        ValidatorUtils.validateEntity(dto);

        //用户登录
        Map<String, Object> map = userService.login(dto);

        return new Result().ok(map);
    }

    @Login
    @PostMapping("logout")
    @Operation(summary = "退出")
    public Result logout(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        tokenService.expireToken(userId);
        return new Result();
    }

}
