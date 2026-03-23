/**
 * Copyright (c) 2016-2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.feign.fallback;

import com.jslh.feign.ParamsFeignClient;
import org.springframework.stereotype.Component;

/**
 * 参数接口 FallbackFactory
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
public class ParamsFeignClientFallbackFactory implements ParamsFeignClient {
    @Override
    public String getValue(String paramCode) {
        return null;
    }

    @Override
    public void updateValueByCode(String paramCode, String paramValue) {

    }
}
