/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.commons.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jslh.commons.mybatis.enums.DelFlagEnum;
import com.jslh.commons.security.context.TenantContext;
import com.jslh.commons.security.user.SecurityUser;
import com.jslh.commons.security.user.UserDetail;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 公共字段，自动填充值
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
public class FieldMetaObjectHandler implements MetaObjectHandler {
    private final static String CREATE_DATE = "createDate";
    private final static String CREATOR = "creator";
    private final static String UPDATE_DATE = "updateDate";
    private final static String UPDATER = "updater";
    private final static String DEL_FLAG = "delFlag";
    private final static String DEPT_ID = "deptId";
    private final static String TENANT_CODE = "tenantCode";

    @Override
    public void insertFill(MetaObject metaObject) {
        UserDetail user = SecurityUser.getUser();
        if (user == null) {
            return;
        }
        Date date = new Date();

        //创建者
        strictInsertFill(metaObject, CREATOR, Long.class, user.getId());
        //创建时间
        strictInsertFill(metaObject, CREATE_DATE, Date.class, date);

        //创建者所属部门
        strictInsertFill(metaObject, DEPT_ID, Long.class, user.getDeptId());

        //租户编码
        if (user.getTenantCode() != null) {
            strictInsertFill(metaObject, TENANT_CODE, Long.class, TenantContext.getTenantCode(user));
        }

        //更新者
        strictInsertFill(metaObject, UPDATER, Long.class, user.getId());
        //更新时间
        strictInsertFill(metaObject, UPDATE_DATE, Date.class, date);

        //删除标识
        strictInsertFill(metaObject, DEL_FLAG, Integer.class, DelFlagEnum.NORMAL.value());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新者
        strictUpdateFill(metaObject, UPDATER, Long.class, SecurityUser.getUserId());
        //更新时间
        strictUpdateFill(metaObject, UPDATE_DATE, Date.class, new Date());
    }
}
