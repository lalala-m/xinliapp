/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.feign;

import com.jslh.commons.tools.utils.Result;
import com.jslh.feign.fallback.StorageFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存 Feign Client
 *
 * @author Mark sunlightcs@gmail.com
 */
@FeignClient(name = "jslh-seata-storage", fallbackFactory = StorageFeignClientFallbackFactory.class)
public interface StorageFeignClient {

	/**
	 * 减库存
	 *
	 * @param commodityCode 商品代码
	 * @param count         数量
	 */
	@PutMapping("seata-storage/storage/deduct")
	Result deduct(@RequestParam("commodityCode") String commodityCode, @RequestParam("count") Integer count);

}
