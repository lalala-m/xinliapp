/**
 * Copyright (c) 2016-2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.feign.fallback;

import com.jslh.commons.tools.utils.Result;
import com.jslh.dto.UploadDTO;
import com.jslh.feign.OssFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * OSS FallbackFactory
 *
 * @author Mark sunlightcs@gmail.com
 */
@Slf4j
@Component
public class OssFeignClientFallbackFactory implements FallbackFactory<OssFeignClient> {
    @Override
    public OssFeignClient create(Throwable throwable) {
        log.error("{}", throwable);

        return file -> new Result<UploadDTO>().error();
    }
}
