package com.jslh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jslh.commons.mybatis.service.impl.BaseServiceImpl;
import com.jslh.commons.tools.utils.ConvertUtils;
import com.jslh.dao.AiChatMessageDao;
import com.jslh.dto.AiChatMessageDTO;
import com.jslh.entity.AiChatMessageEntity;
import com.jslh.service.AiChatMessageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
@AllArgsConstructor
public class AiChatMessageServiceImpl extends BaseServiceImpl<AiChatMessageDao, AiChatMessageEntity> implements AiChatMessageService {

    @Override
    public List<AiChatMessageDTO> getList(Long conversationId) {
        LambdaQueryWrapper<AiChatMessageEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiChatMessageEntity::getConversationId, conversationId);
        List<AiChatMessageEntity> list = baseDao.selectList(wrapper);

        return ConvertUtils.sourceToTarget(list, AiChatMessageDTO.class);
    }

    @Override
    public void clearList(Long conversationId) {
        LambdaQueryWrapper<AiChatMessageEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiChatMessageEntity::getConversationId, conversationId);
        baseDao.delete(wrapper);
    }
}
