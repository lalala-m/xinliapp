/**
 * Copyright (c) 2019 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.commons.security.context;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.jslh.commons.security.user.UserDetail;
import com.jslh.commons.tools.enums.SuperAdminEnum;
import com.jslh.commons.tools.utils.HttpContextUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 租户
 *
 * @author Mark sunlightcs@gmail.com
 */
public class TenantContext {
    /**
     * 默认租户编码
     */
    private static final Long DEFAULT_TENANT_CODE = 10000L;
    /**
     * 当前访问者租户编码
     */
    private static final ThreadLocal<Long> VISITOR_TENANT_CODE = new TransmittableThreadLocal<>();

    /**
     * 当前访问者租户编码
     */
    public static void setVisitorTenantCode(Long tenantCode) {
        VISITOR_TENANT_CODE.set(tenantCode);
    }

    /**
     * 当前访问者租户编码
     */
    public static Long getVisitorTenantCode() {
        return VISITOR_TENANT_CODE.get();
    }


    public static Long getTenantCode(UserDetail user) {
        String tenantCode = HttpContextUtils.getTenantCode();

        // 默认租户的超级管理员，才可以切换租户
        if (ObjectUtil.equals(user.getSuperAdmin(), SuperAdminEnum.YES.value())
                && ObjectUtil.equals(user.getTenantCode(), DEFAULT_TENANT_CODE)) {
            if (StringUtils.isNotBlank(tenantCode)) {
                return Long.parseLong(tenantCode);
            }
        }

        if (user.getTenantCode() != null) {
            return user.getTenantCode();
        }

        if (StringUtils.isBlank(tenantCode)) {
            return DEFAULT_TENANT_CODE;
        }

        return Long.parseLong(tenantCode);
    }
}
