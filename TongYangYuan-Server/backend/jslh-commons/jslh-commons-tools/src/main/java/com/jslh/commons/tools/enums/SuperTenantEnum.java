/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.commons.tools.enums;

/**
 * 租户管理员枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum SuperTenantEnum {
    YES(1),
    NO(0);

    private int value;

    SuperTenantEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
