/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.controller;

import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.Result;
import com.jslh.config.DataSourceInfo;
import com.jslh.entity.DataSourceEntity;
import com.jslh.service.DataSourceService;
import com.jslh.utils.DbUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 数据源管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("datasource")
public class DataSourceController {
    @Resource
    private DataSourceService datasourceService;

    @GetMapping("page")
    public Result<PageData<DataSourceEntity>> page(@RequestParam Map<String, Object> params) {
        PageData<DataSourceEntity> page = datasourceService.page(params);

        return new Result<PageData<DataSourceEntity>>().ok(page);
    }

    @GetMapping("list")
    public Result<List<DataSourceEntity>> list() {
        List<DataSourceEntity> list = datasourceService.list();

        return new Result<List<DataSourceEntity>>().ok(list);
    }

    @GetMapping("{id}")
    public Result<DataSourceEntity> get(@PathVariable("id") Long id) {
        DataSourceEntity data = datasourceService.selectById(id);

        return new Result<DataSourceEntity>().ok(data);
    }

    @GetMapping("test/{id}")
    public Result<String> test(@PathVariable("id") Long id) {
        try {
            DataSourceEntity entity = datasourceService.selectById(id);

            DbUtils.getConnection(new DataSourceInfo(entity));
            return new Result<String>().ok("连接成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<String>().error("连接失败，请检查配置信息");
        }
    }

    @PostMapping
    public Result save(@RequestBody DataSourceEntity entity) {
        datasourceService.insert(entity);

        return new Result();
    }

    @PutMapping
    public Result update(@RequestBody DataSourceEntity entity) {
        datasourceService.updateById(entity);

        return new Result();
    }

    @DeleteMapping
    public Result delete(@RequestBody Long[] ids) {
        datasourceService.deleteBatchIds(Arrays.asList(ids));

        return new Result();
    }
}
