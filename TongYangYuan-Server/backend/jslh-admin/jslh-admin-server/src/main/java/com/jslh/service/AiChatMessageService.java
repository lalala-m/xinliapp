package com.jslh.service;


import com.jslh.commons.mybatis.service.BaseService;
import com.jslh.dto.AiChatMessageDTO;
import com.jslh.entity.AiChatMessageEntity;

import java.util.List;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AiChatMessageService extends BaseService<AiChatMessageEntity> {

    List<AiChatMessageDTO> getList(Long conversationId);

    void clearList(Long conversationId);

}
