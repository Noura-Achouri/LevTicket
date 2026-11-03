package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    void insert(Notification notification);
    List<Notification> findByUserId(Integer userId);
    int countUnread(Integer userId);
    void markRead(@Param("id") Long id, @Param("userId") Integer userId);
    void markAllRead(Integer userId);
}
