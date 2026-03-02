/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.sms;

import cn.hutool.core.map.MapUtil;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.commons.tools.utils.SpringContextUtils;
import com.jslh.dto.SmsConfig;
import com.jslh.enums.PlatformEnum;
import com.jslh.exception.ModuleErrorCode;
import com.jslh.service.SysSmsLogService;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 腾讯云短信服务
 *
 * @author Mark sunlightcs@gmail.com
 */
public class QcloudSmsService extends AbstractSmsService {
    public QcloudSmsService(SmsConfig config){
        this.config = config;
    }

    @Override
    public void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params) {
        this.sendSms(smsCode, mobile, params, config.getQcloudSignName(), config.getQcloudTemplateId());
    }

    @Override
    public void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params, String signName, String template) {
        SmsSingleSender sender = new SmsSingleSender(config.getQcloudAppId(), config.getQcloudAppKey());

        //短信参数
        ArrayList<String> paramsList = new ArrayList<>();
        if(MapUtil.isNotEmpty(params)){
            for(String value : params.values()){
                paramsList.add(value);
            }
        }
        SmsSingleSenderResult result;
        try {
            result = sender.sendWithParam("86", mobile, Integer.parseInt(template), paramsList, signName, null, null);
        } catch (Exception e) {
            throw new RenException(ModuleErrorCode.SEND_SMS_ERROR, e, "");
        }

        int status = Constant.SUCCESS;
        if(result.result != 0){
            status = Constant.FAIL;
        }

        //保存短信记录
        SysSmsLogService sysSmsLogService = SpringContextUtils.getBean(SysSmsLogService.class);
        sysSmsLogService.save(smsCode, PlatformEnum.QINIU.value(), mobile, params, status);

        if(status == Constant.FAIL){
            throw new RenException(ModuleErrorCode.SEND_SMS_ERROR, result.errMsg);
        }
    }
}
