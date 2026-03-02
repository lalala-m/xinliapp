package com.jslh.service;


import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.dto.MpMenuDTO;
import com.jslh.entity.MpMenuEntity;

/**
 * 公众号自定义菜单
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface MpMenuService extends CrudService<MpMenuEntity, MpMenuDTO> {

    MpMenuDTO getByAppId(String appId);

    void deleteByAppId(String appId);
}
