/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.commons.security.context.TenantContext;
import com.jslh.commons.security.user.SecurityUser;
import com.jslh.commons.security.user.UserDetail;
import com.jslh.commons.tools.constant.Constant;
import com.jslh.commons.tools.enums.SuperAdminEnum;
import com.jslh.commons.tools.enums.SuperTenantEnum;
import com.jslh.commons.tools.exception.RenException;
import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.ConvertUtils;
import com.jslh.dao.SysUserDao;
import com.jslh.dto.SysUserDTO;
import com.jslh.entity.SysUserEntity;
import com.jslh.service.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Service
public class SysUserServiceImpl extends BaseServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {
    @Resource
    private SysRoleUserService sysRoleUserService;
    @Resource
    private SysDeptService sysDeptService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private SysUserPostService sysUserPostService;
    @Resource
    private SysUserTokenService sysUserTokenService;

    @Override
    public PageData<SysUserDTO> page(Map<String, Object> params) {
        //转换成like
        paramsToLike(params, "username");

        //分页
        IPage<SysUserEntity> page = getPage(params, "t1.create_date", false);

        //查询
        List<SysUserEntity> list = baseDao.getList(getQueryParams(params));

        return getPageData(list, page.getTotal(), SysUserDTO.class);
    }

    @Override
    public List<SysUserDTO> list(Map<String, Object> params) {
        List<SysUserEntity> entityList = baseDao.getList(getQueryParams(params));

        return ConvertUtils.sourceToTarget(entityList, SysUserDTO.class);
    }

    private Map<String, Object> getQueryParams(Map<String, Object> params) {
        //普通管理员，只能查询所属部门及子部门的数据
        UserDetail user = SecurityUser.getUser();
        if (user.getSuperAdmin() == SuperAdminEnum.NO.value()
                && user.getSuperTenant() == SuperTenantEnum.NO.value()) {
            params.put("deptIdList", sysDeptService.getSubDeptIdList(user.getDeptId()));
        }

        //租户
        params.put(Constant.TENANT_CODE, TenantContext.getTenantCode(user));

        return params;
    }

    @Override
    public SysUserDTO get(Long id) {
        SysUserEntity entity = baseDao.getById(id);

        return ConvertUtils.sourceToTarget(entity, SysUserDTO.class);
    }

    @Override
    public SysUserDTO getByUsername(String username) {
        SysUserEntity entity = baseDao.getByUsername(username, TenantContext.getVisitorTenantCode());
        return ConvertUtils.sourceToTarget(entity, SysUserDTO.class);
    }

    public void save(SysUserDTO dto) {
        SysUserEntity entity = ConvertUtils.sourceToTarget(dto, SysUserEntity.class);

        // 校验用户名是否存在
        SysUserDTO dbUser = getByUsername(entity.getUsername());
        if (dbUser != null) {
            throw new RenException("用户名存在，请修改其他用户名");
        }

        //密码加密
        String password = passwordEncoder.encode(entity.getPassword());
        entity.setPassword(password);

        //保存用户
        entity.setSuperTenant(SuperTenantEnum.NO.value());
        entity.setSuperAdmin(SuperAdminEnum.NO.value());
        entity.setTenantCode(TenantContext.getTenantCode(SecurityUser.getUser()));
        insert(entity);

        //保存角色用户关系
        sysRoleUserService.saveOrUpdate(entity.getId(), dto.getRoleIdList());

        //保存用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), dto.getPostIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserDTO dto) {
        SysUserEntity entity = ConvertUtils.sourceToTarget(dto, SysUserEntity.class);

        //密码加密
        if (StringUtils.isBlank(dto.getPassword())) {
            entity.setPassword(null);
        } else {
            String password = passwordEncoder.encode(entity.getPassword());
            entity.setPassword(password);
        }

        //更新用户
        updateById(entity);

        //更新角色用户关系
        sysRoleUserService.saveOrUpdate(entity.getId(), dto.getRoleIdList());

        //保存用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), dto.getPostIdList());

        //更新用户缓存权限
        sysUserTokenService.updateCacheAuthByUserId(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(SysUserDTO dto) {
        SysUserEntity entity = selectById(dto.getId());
        entity.setHeadUrl(dto.getHeadUrl());
        entity.setRealName(dto.getRealName());
        entity.setGender(dto.getGender());
        entity.setMobile(dto.getMobile());
        entity.setEmail(dto.getEmail());

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        //逻辑删除
        logicDelete(ids, SysUserEntity.class);

        //角色用户关系，岗位关系需要保留，不然逻辑删除就变成物理删除了
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long id, String newPassword) {
        newPassword = passwordEncoder.encode(newPassword);

        baseDao.updatePassword(id, newPassword);
    }

    @Override
    public int getCountByDeptId(Long deptId) {
        return baseDao.getCountByDeptId(deptId);
    }

    @Override
    public List<Long> getUserIdListByDeptId(List<Long> deptIdList) {
        return baseDao.getUserIdListByDeptId(deptIdList);
    }

    @Override
    public List<String> getRealNameList(List<Long> ids) {
        return baseDao.getRealNameList(ids);
    }

    @Override
    public List<Long> getUserIdListByRoleIdList(List<Long> ids) {
        return baseDao.getUserIdListByRoleIdList(ids);
    }

    @Override
    public List<String> getRoleNameList(List<Long> ids) {
        return baseDao.getRoleNameList(ids);
    }

    @Override
    public List<Long> getUserIdListByPostIdList(List<Long> ids) {
        return baseDao.getUserIdListByPostIdList(ids);
    }

    @Override
    public List<Long> getLeaderIdListByDeptIdList(List<Long> ids) {
        return baseDao.getLeaderIdListByDeptIdList(ids);
    }

    @Override
    public Long getLeaderIdListByUserId(Long userId) {
        return baseDao.getLeaderIdListByUserId(userId);
    }

}
