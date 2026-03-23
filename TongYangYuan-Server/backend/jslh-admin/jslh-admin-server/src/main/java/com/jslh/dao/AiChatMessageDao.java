package com.jslh.dao;


import com.jslh.commons.mybatis.dao.BaseDao;
import com.jslh.entity.AiChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface AiChatMessageDao extends BaseDao<AiChatMessageEntity> {

}
