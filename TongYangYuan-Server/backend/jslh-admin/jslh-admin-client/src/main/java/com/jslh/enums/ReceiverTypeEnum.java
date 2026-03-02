/**
 * Copyright (c) 2016-2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.enums;

/**
 * 接受者类型枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum ReceiverTypeEnum {
    /**
     * 全部
     */
    ALL(0),
    /**
     * 部门
     */
    DEPT(1);

    private final int value;

    ReceiverTypeEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
