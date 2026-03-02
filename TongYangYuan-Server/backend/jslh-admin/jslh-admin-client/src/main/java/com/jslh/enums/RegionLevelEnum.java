/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.enums;

/**
 * 行政区域  级别枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum RegionLevelEnum {
    ONE(1),
    TWO(2),
    THREE(3);

    private final int value;

    RegionLevelEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
