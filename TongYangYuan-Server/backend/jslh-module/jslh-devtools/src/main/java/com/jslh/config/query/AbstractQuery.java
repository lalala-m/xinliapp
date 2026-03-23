/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.config.query;


import com.jslh.utils.DbType;

/**
 * Query
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AbstractQuery {

    /**
     * 数据库类型
     */
    DbType dbType();

    /**
     * 表信息查询 SQL
     */
    String tablesSql(String tableName);

    /**
     * 表名称
     */
    String tableName();

    /**
     * 表注释
     */
    String tableComment();

    /**
     * 表字段信息查询 SQL
     */
    String tableFieldsSql();

    /**
     * 字段名称
     */
    String fieldName();


    /**
     * 字段类型
     */
    String fieldType();


    /**
     * 字段注释
     */
    String fieldComment();

    /**
     * 主键字段
     */
    String fieldKey();
}
