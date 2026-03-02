/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.commons.tools.enums;

/**
 * 删除标记枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum DeleteEnum {
    /**
     * 是（删除）
     */
    YES(1),
    /**
     * 否（未删除）
     */
    NO(0);

    private int value;

    DeleteEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
