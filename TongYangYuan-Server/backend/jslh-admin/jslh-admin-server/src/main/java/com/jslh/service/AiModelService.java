package com.jslh.service;


import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.commons.tools.page.PageData;
import com.jslh.dto.AiModelDTO;
import com.jslh.entity.AiModelEntity;

import java.util.List;
import java.util.Map;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AiModelService extends BaseService<AiModelEntity> {

    PageData<AiModelDTO> page(Map<String, Object> params);

    List<AiModelDTO> getList();

    AiModelDTO get(Long id);

    void save(AiModelDTO dto);

    void update(AiModelDTO dto);

    void delete(List<Long> idList);

}
