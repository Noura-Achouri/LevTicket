package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.dto.ChatConversation;
import com.leviathan.bidpass.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {

    void insert(ChatMessage message);

    ChatMessage findById(Long id);

    /** Full thread between two users, oldest first. */
    List<ChatMessage> findThread(@Param("userId") Integer userId, @Param("partnerId") Integer partnerId);

    /** One row per chat partner, carrying the last message and unread count. */
    List<ChatConversation> findConversations(Integer userId);

    int countUnread(Integer userId);

    int countUnreadFrom(@Param("receiverId") Integer receiverId, @Param("senderId") Integer senderId);

    int markThreadRead(@Param("userId") Integer userId, @Param("partnerId") Integer partnerId);
}
