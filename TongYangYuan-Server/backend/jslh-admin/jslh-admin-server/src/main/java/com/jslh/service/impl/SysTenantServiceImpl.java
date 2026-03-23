/**
 * Copyright (c) 2018 晶石领航 All rights reserved.
 * <p>
 * https://www.jslh.com
 * <p>
 * 版权所有，侵权必究！
 */

package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jslh.commons.dynamic.datasource.config.DynamicContextHolder;
import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.commons.security.enums.UserStatusEnum;
import com.jslh.commons.tools.enums.DeleteEnum;
import com.jslh.commons.tools.enums.SuperAdminEnum;
import com.jslh.commons.tools.enums.SuperTenantEnum;
import com.jslh.commons.tools.enums.TenantModeEnum;
import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.ConvertUtils;
import com.jslh.dao.SysTenantDao;
import com.jslh.dao.SysUserDao;
import com.jslh.entity.SysTenantEntity;
import com.jslh.entity.SysUserEntity;
import com.jslh.service.SysRoleUserService;
import com.jslh.service.SysTenantService;
import com.jslh.tenant.dto.SysTenantDTO;
import com.jslh.tenant.dto.SysTenantListDTO;
import com.jslh.tenant.redis.SysTenantRedis;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class SysTenantServiceImpl extends BaseServiceImpl<SysTenantDao, SysTenantEntity> implements SysTenantService {
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private SysTenantRedis sysTenantRedis;
    @Resource
    private SysRoleUserService sysRoleUserService;

    @Override
    public PageData<SysTenantDTO> page(Map<String, Object> params) {
        //转换成like
        paramsToLike(params, "tenantName");

        //分页
        IPage<SysTenantEntity> page = getPage(params, "create_date", false);

        //查询
        List<SysTenantEntity> list = baseDao.getList(params);

        return getPageData(list, page.getTotal(), SysTenantDTO.class);
    }

    @Override
    public synchronized List<SysTenantListDTO> list() {
        List<SysTenantListDTO> dtoList = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        params.put("status", 1);
        List<SysTenantEntity> list = baseDao.getList(params);
        for (SysTenantEntity entity : list) {
            SysTenantListDTO dto = new SysTenantListDTO();
            dto.setTenantCode(entity.getId());
            dto.setTenantName(entity.getTenantName());
            dto.setTenantDomain(entity.getTenantDomain());

            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public SysTenantDTO get(Long id) {
        SysTenantEntity entity = baseDao.getById(id);

        return ConvertUtils.sourceToTarget(entity, SysTenantDTO.class);
    }

    @Override
    public void save(SysTenantDTO dto) {
        SysTenantEntity entity = ConvertUtils.sourceToTarget(dto, SysTenantEntity.class);
        // 保存租户
        entity.setDelFlag(DeleteEnum.NO.value());
        insert(entity);

        SysUserEntity user = new SysUserEntity();
        user.setSuperAdmin(SuperAdminEnum.NO.value());

        // 切换数据源
        if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
            DynamicContextHolder.push(entity.getDatasourceId() + "");

            user.setSuperAdmin(SuperAdminEnum.YES.value());
        }

        // 保存用户
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setSuperTenant(SuperTenantEnum.YES.value());
        user.setStatus(UserStatusEnum.ENABLED.value());
        user.setTenantCode(entity.getId());
        user.setGender(2);

        sysUserDao.insert(user);

        // 清除数据源
        if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
            DynamicContextHolder.poll();
        }

        // 字段模式
        if (dto.getTenantMode() == TenantModeEnum.COLUMN.value()) {
            // 保存角色用户关系
            sysRoleUserService.saveOrUpdate(user.getId(), dto.getRoleIdList());
        }

        // 更新用户ID
        entity.setUserId(user.getId());
        baseDao.updateById(entity);

        // 清空缓存
        sysTenantRedis.clear();
    }

    @Override
    public void update(SysTenantDTO dto) {
        //更新租户
        SysTenantEntity entity = ConvertUtils.sourceToTarget(dto, SysTenantEntity.class);
        updateById(entity);

        // 查询租户用户ID
        Long userId = baseDao.selectById(entity.getId()).getUserId();

        // 修改租户用户密码
        if (StringUtils.isNotBlank(dto.getPassword())) {
            // 切换数据源
            if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
                DynamicContextHolder.push(entity.getDatasourceId() + "");
            }

            // 修改密码
            sysUserDao.updatePassword(userId, passwordEncoder.encode(dto.getPassword()));

            // 清除数据源
            if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
                DynamicContextHolder.poll();
            }

        }

        // 字段模式
        if (dto.getTenantMode() == TenantModeEnum.COLUMN.value()) {
            // 保存角色用户关系
            sysRoleUserService.saveOrUpdate(userId, dto.getRoleIdList());
        }

        // 清空缓存
        sysTenantRedis.clear();
    }

    @Override
    public void delete(Long[] ids) {
        baseDao.deleteBatch(ids);

        // 清空缓存
        sysTenantRedis.clear();
    }
}
