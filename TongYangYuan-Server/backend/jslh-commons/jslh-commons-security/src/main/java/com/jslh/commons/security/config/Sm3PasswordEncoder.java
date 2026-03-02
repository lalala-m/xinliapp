package com.jslh.commons.security.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 国密SM3加密算法，对系统密码进行加密
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Sm3PasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        return SmUtil.sm3(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return StrUtil.equals(SmUtil.sm3(rawPassword.toString()), encodedPassword);
    }

    public static void main(String[] args) {
    	System.out.println(new Sm3PasswordEncoder().encode("admin"));
    }
}
