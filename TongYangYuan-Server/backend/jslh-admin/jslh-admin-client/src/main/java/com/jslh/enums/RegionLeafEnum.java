/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.enums;

/**
 * 叶子节点枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum RegionLeafEnum {
    YES(1),
    NO(0);

    private final int value;

    RegionLeafEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
