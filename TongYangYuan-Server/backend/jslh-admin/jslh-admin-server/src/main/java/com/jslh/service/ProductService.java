package com.jslh.service;

import com.jslh.commons.mybatis.service.CrudService;
import com.jslh.dto.ProductDTO;
import com.jslh.entity.ProductEntity;

/**
 * 产品管理
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface ProductService extends CrudService<ProductEntity, ProductDTO> {

}
