/**
 * Copyright (c) 2016-2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.feign.fallback;

import com.jslh.commons.tools.utils.Result;
import com.jslh.feign.DictFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 字典接口 FallbackFactory
 *
 * @author Mark sunlightcs@gmail.com
 */
@Slf4j
@Component
public class DictFeignClientFallbackFactory implements FallbackFactory<DictFeignClient> {
    @Override
    public DictFeignClient create(Throwable throwable) {
        log.error("{}", throwable);
        return () -> new Result<>();
    }
}
