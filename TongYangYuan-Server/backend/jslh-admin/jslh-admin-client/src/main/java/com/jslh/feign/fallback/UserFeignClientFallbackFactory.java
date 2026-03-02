/**
 * Copyright (c) 2016-2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.feign.fallback;

import com.jslh.commons.security.user.UserDetail;
import com.jslh.commons.tools.utils.Result;
import com.jslh.feign.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户接口 FallbackFactory
 *
 * @author Mark sunlightcs@gmail.com
 */
@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable throwable) {
        log.error("{}", throwable);

        return new UserFeignClient() {
            @Override
            public Result<UserDetail> getById(Long id) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getRoleIdList(Long userId) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getUserIdListByRoleIdList(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getUserIdListByPostIdList(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getLeaderIdListByDeptIdList(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<Long> getLeaderIdListByUserId(Long userId) {
                return new Result<>();
            }
        };
    }
}
