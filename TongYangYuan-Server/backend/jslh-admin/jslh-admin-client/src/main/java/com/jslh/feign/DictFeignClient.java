/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.feign;

import com.jslh.commons.tools.constant.ServiceConstant;
import com.jslh.commons.tools.utils.Result;
import com.jslh.dto.SysDictTypeDTO;
import com.jslh.feign.fallback.DictFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 字典接口
 *
 * @author Mark sunlightcs@gmail.com
 */
@FeignClient(name = ServiceConstant.JSLH_ADMIN_SERVER, contextId = "DictFeignClient", fallbackFactory = DictFeignClientFallbackFactory.class)
public interface DictFeignClient {

    /**
     * 字典类型列表
     */
    @GetMapping("sys/dict/type/list")
    Result<List<SysDictTypeDTO>> getDictTypeList();

}
