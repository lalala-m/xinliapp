package com.jslh.service;

import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.dto.AiChatConversationDTO;
import com.jslh.entity.AiChatConversationEntity;

import java.util.List;

/**
 * AI聊天对话
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AiChatConversationService extends BaseService<AiChatConversationEntity> {

    List<AiChatConversationDTO> getList();

    AiChatConversationDTO save(AiChatConversationDTO dto);

    void update(AiChatConversationDTO dto);

    void delete(List<Long> idList);

}
