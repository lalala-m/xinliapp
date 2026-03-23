/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.entity;

import lombok.Data;

/**
 * 创建菜单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class MenuEntity {
    private Long pid;
    private String name;
    private String icon;
    private String backendUrl;
    private String className;

}
