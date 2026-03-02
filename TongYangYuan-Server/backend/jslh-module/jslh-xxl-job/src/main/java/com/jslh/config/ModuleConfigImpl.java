/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.config;

import com.jslh.commons.tools.config.ModuleConfig;
import org.springframework.stereotype.Service;

/**
 * 模块配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class ModuleConfigImpl implements ModuleConfig {
    @Override
    public String getName() {
        return "xxl-job";
    }
}
