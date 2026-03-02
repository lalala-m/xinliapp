/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.sms;

import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.util.Auth;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.commons.tools.utils.SpringContextUtils;
import com.jslh.dto.SmsConfig;
import com.jslh.enums.PlatformEnum;
import com.jslh.exception.ModuleErrorCode;
import com.jslh.service.SysSmsLogService;

import java.util.LinkedHashMap;

/**
 * 七牛短信服务
 *
 * @author Mark sunlightcs@gmail.com
 */
public class QiniuSmsService extends AbstractSmsService {
    private SmsManager smsManager;

    public QiniuSmsService(SmsConfig config){
        this.config = config;

        //初始化
        init();
    }


    private void init(){
        Auth auth = Auth.create(config.getQiniuAccessKey(), config.getQiniuSecretKey());
        smsManager = new SmsManager(auth);
    }

    @Override
    public void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params) {
        this.sendSms(smsCode, mobile, params, null, config.getQiniuTemplateId());
    }

    @Override
    public void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params, String signName, String template) {
        Response response;
        try {
            response = smsManager.sendSingleMessage(template, mobile, params);
        } catch (Exception e) {
            throw new RenException(ModuleErrorCode.SEND_SMS_ERROR, e, "");
        }

        int status = Constant.SUCCESS;
        if(!response.isOK()){
            status = Constant.FAIL;
        }

        //保存短信记录
        SysSmsLogService sysSmsLogService = SpringContextUtils.getBean(SysSmsLogService.class);
        sysSmsLogService.save(smsCode, PlatformEnum.QINIU.value(), mobile, params, status);

        if(status == Constant.FAIL){
            throw new RenException(ModuleErrorCode.SEND_SMS_ERROR, response.error);
        }
    }
}
