/**
 * Copyright (c) 2020 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */
package com.jslh.security.service;

import com.jslh.commons.security.enums.UserStatusEnum;
import com.jslh.commons.security.user.UserDetail;
import com.jslh.commons.tools.exception.ErrorCode;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.service.SysUserDetailService;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class RenUserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private SysUserDetailService sysUserDetailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetail userDetail = sysUserDetailService.getUserDetailByUsername(username);
        if (userDetail == null) {
            throw new RenException(ErrorCode.ACCOUNT_NOT_EXIST);
        }

        // 账号不可用
        if (userDetail.getStatus() == UserStatusEnum.DISABLE.value()) {
            userDetail.setEnabled(false);
        }

        return userDetail;
    }
}
