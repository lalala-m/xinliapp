/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.commons.log;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 登录日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysLogLogin extends BaseLog {
    private static final long serialVersionUID = 1L;
    /**
     * 用户操作
     */
    private Integer operation;
    /**
     * 用户代理
     */
    private String userAgent;
    /**
     * 操作IP
     */
    private String ip;
    /**
     * 用户名
     */
    private String creatorName;
    /**
     * 创建者
     */
    private Long creator;
    /**
     * 创建时间
     */
    private Date createDate;

}
