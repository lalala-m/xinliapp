/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传信息
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.1.0
 */
@Data
@Schema(description = "上传信息")
public class UploadDTO {
    @Schema(description = "文件URL")
    private String url;
    @Schema(description = "文件大小，单位字节")
    private Long size;

}
