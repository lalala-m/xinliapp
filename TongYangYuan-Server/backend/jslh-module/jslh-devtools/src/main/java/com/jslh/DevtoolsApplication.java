/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 代码生成器模块
 *
 * @author Mark sunlightcs@gmail.com
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DevtoolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevtoolsApplication.class, args);
    }

}
