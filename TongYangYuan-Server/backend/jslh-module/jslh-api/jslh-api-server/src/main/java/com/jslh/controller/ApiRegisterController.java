/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.controller;

import com.jslh.commons.tools.utils.Result;
import com.jslh.commons.tools.validator.ValidatorUtils;
import com.jslh.dto.RegisterDTO;
import com.jslh.entity.UserEntity;
import com.jslh.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 注册接口
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("register")
@Tag(name = "注册接口")
public class ApiRegisterController {
    @Resource
    private UserService userService;

    @PostMapping
    @Operation(summary = "注册")
    public Result register(@RequestBody RegisterDTO dto) {
        //表单校验
        ValidatorUtils.validateEntity(dto);

        UserEntity user = new UserEntity();
        user.setMobile(dto.getMobile());
        user.setUsername(dto.getMobile());
        user.setPassword(DigestUtils.sha256Hex(dto.getPassword()));
        user.setCreateDate(new Date());
        userService.insert(user);

        return new Result();
    }
}
