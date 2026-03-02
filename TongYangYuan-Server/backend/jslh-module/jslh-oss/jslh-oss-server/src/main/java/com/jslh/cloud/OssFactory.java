/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 *
 * https://www.jslh.com
 *
 * 版权所有，侵权必究！
 */

package com.jslh.cloud;

import com.jslh.commons.tools.utils.SpringContextUtils;
import com.jslh.enums.OssTypeEnum;
import com.jslh.remote.ParamsRemoteService;
import com.jslh.utils.ModuleConstant;

/**
 * 文件上传Factory
 *
 * @author Mark sunlightcs@gmail.com
 */
public final class OssFactory {
    private static ParamsRemoteService paramsRemoteService;

    static {
        OssFactory.paramsRemoteService = SpringContextUtils.getBean(ParamsRemoteService.class);
    }

    public static AbstractCloudStorageService build(){
        //获取云存储配置信息
        CloudStorageConfig config = paramsRemoteService.getValueObject(ModuleConstant.CLOUD_STORAGE_CONFIG_KEY, CloudStorageConfig.class);

        if(config.getType() == OssTypeEnum.QINIU.value()){
            return new QiniuCloudStorageService(config);
        }else if(config.getType() == OssTypeEnum.ALIYUN.value()){
            return new AliyunCloudStorageService(config);
        }else if(config.getType() == OssTypeEnum.QCLOUD.value()){
            return new QcloudCloudStorageService(config);
        }else if(config.getType() == OssTypeEnum.FASTDFS.value()){
            return new FastDFSCloudStorageService(config);
        }else if(config.getType() == OssTypeEnum.LOCAL.value()){
            return new LocalCloudStorageService(config);
        }else if(config.getType() == OssTypeEnum.MINIO.value()){
            return new MinioCloudStorageService(config);
        }

        return null;
    }

}
