/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.enums;

/**
 * 菜单资源标识
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
public enum MenuFlagEnum {
    /**
     * 菜单资源
     */
    YES(1),
    /**
     * 非菜单资源
     */
    NO(0);

    private final int value;

    MenuFlagEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
