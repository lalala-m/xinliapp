package com.jslh.service;


import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.dto.ProductParamsDTO;
import com.jslh.entity.ProductParamsEntity;

import java.util.List;

/**
 * 产品参数管理
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface ProductParamsService extends BaseService<ProductParamsEntity> {

    void saveOrUpdate(Long productId, List<ProductParamsDTO> list);

    void deleteByProductIds(Long[] productIds);

    List<ProductParamsDTO> getList(Long productId);
}
