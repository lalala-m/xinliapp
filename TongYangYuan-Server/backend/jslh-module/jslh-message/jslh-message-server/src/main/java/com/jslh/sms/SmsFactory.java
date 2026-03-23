/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.sms;

import com.jslh.commons.tools.utils.JsonUtils;
import com.jslh.commons.tools.utils.SpringContextUtils;
import com.jslh.dto.SmsConfig;
import com.jslh.entity.SysSmsEntity;
import com.jslh.enums.PlatformEnum;
import com.jslh.service.SysSmsService;

/**
 * 短信Factory
 *
 * @author Mark sunlightcs@gmail.com
 */
public class SmsFactory {
    private static SysSmsService sysSmsService;

    static {
        SmsFactory.sysSmsService = SpringContextUtils.getBean(SysSmsService.class);
    }

    public static AbstractSmsService build(String smsCode){
        //获取短信配置信息
        SysSmsEntity smsEntity = sysSmsService.getBySmsCode(smsCode);
        SmsConfig config = JsonUtils.parseObject(smsEntity.getSmsConfig(), SmsConfig.class);

        if(smsEntity.getPlatform() == PlatformEnum.ALIYUN.value()){
            return new AliyunSmsService(config);
        }else if(smsEntity.getPlatform() == PlatformEnum.QCLOUD.value()){
            return new QcloudSmsService(config);
        }else if(smsEntity.getPlatform() == PlatformEnum.QINIU.value()){
            return new QiniuSmsService(config);
        }

        return null;
    }
}
