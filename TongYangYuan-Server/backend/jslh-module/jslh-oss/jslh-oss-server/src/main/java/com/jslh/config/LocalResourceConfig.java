package com.jslh.config;

import com.jslh.cloud.CloudStorageConfig;
import com.jslh.commons.tools.utils.SpringContextUtils;
import com.jslh.enums.OssTypeEnum;
import com.jslh.remote.ParamsRemoteService;
import com.jslh.utils.ModuleConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地资源文件映射配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Configuration
public class LocalResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ParamsRemoteService paramsRemoteService = SpringContextUtils.getBean(ParamsRemoteService.class);
        // 获取云存储配置信息
        CloudStorageConfig config = paramsRemoteService.getValueObject(ModuleConstant.CLOUD_STORAGE_CONFIG_KEY, CloudStorageConfig.class);

        // 如果不是本地存储，则返回
        if (config.getType() != OssTypeEnum.LOCAL.value()) {
            return;
        }

        registry.addResourceHandler("/" + config.getLocalPrefix() + "/**")
                .addResourceLocations("file:" + config.getLocalPath() + "/" + config.getLocalPrefix() + "/");
    }
}
