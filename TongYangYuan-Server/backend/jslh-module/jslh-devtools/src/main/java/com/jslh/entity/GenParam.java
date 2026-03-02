/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 代码生成参数配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class GenParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String packageName;
    private String version;
    private String author;
    private String email;
    private String backendPath;
    private String frontendPath;
}
