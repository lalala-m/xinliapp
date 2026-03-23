/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */
package com.jslh.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库存表
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("seata_storage")
public class StorageEntity {
	private static final long serialVersionUID = 1L;

	/**
	* id
	*/
	private Long id;
	/**
	* 商品编码
	*/
	private String commodityCode;
	/**
	* 商品库存数
	*/
	private Integer total;
}
