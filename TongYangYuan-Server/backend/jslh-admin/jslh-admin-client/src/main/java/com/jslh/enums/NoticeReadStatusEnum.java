/**
 * Copyright (c) 2016-2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.enums;

/**
 * 通知阅读状态枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum NoticeReadStatusEnum {
    /**
     * 未读
     */
    UNREAD(0),
    /**
     * 已读
     */
    READ(1);

    private final int value;

    NoticeReadStatusEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
