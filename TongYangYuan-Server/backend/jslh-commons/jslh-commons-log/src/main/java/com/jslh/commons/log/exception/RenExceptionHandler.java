/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.commons.log.exception;

import cn.hutool.core.map.MapUtil;
import com.jslh.commons.log.SysLogError;
import com.jslh.commons.log.enums.LogTypeEnum;
import com.jslh.commons.log.producer.LogProducer;
import com.jslh.commons.tools.config.ModuleConfig;
import com.jslh.commons.tools.exception.ErrorCode;
import com.jslh.commons.tools.exception.ExceptionUtils;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.commons.tools.utils.HttpContextUtils;
import com.jslh.commons.tools.utils.IpUtils;
import com.jslh.commons.tools.utils.JsonUtils;
import com.jslh.commons.tools.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;
import java.util.Map;


/**
 * 异常处理器
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@RestControllerAdvice
public class RenExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RenExceptionHandler.class);
    @Resource
    private ModuleConfig moduleConfig;
    @Resource
    private LogProducer logProducer;

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(RenException.class)
    public Result handleRRException(RenException ex) {
        Result result = new Result();
        result.error(ex.getCode(), ex.getMsg());

        return result;
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException ex) {
        Result result = new Result();
        result.error(ErrorCode.DB_RECORD_EXISTS);

        return result;
    }


    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(Exception ex) {
        Result result = new Result();
        result.error(ErrorCode.FORBIDDEN);

        return result;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404 Not Found: " + e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        logger.error(ex.getMessage(), ex);

        saveLog(ex);

        return new Result().error();
    }

    /**
     * 保存异常日志
     */
    private void saveLog(Exception ex) {
        SysLogError log = new SysLogError();
        log.setType(LogTypeEnum.ERROR.value());
        log.setModule(moduleConfig.getName());

        //请求相关信息
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        log.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        log.setRequestUri(request.getRequestURI());
        log.setRequestMethod(request.getMethod());
        log.setIp(IpUtils.getIpAddr(request));
        Map<String, String> params = HttpContextUtils.getParameterMap(request);
        if (MapUtil.isNotEmpty(params)) {
            log.setRequestParams(JsonUtils.toJsonString(params));
        }

        //异常信息
        log.setErrorInfo(ExceptionUtils.getErrorStackTrace(ex));

        //保存到Redis队列里
        log.setCreateDate(new Date());
        logProducer.saveLog(log);
    }
}
