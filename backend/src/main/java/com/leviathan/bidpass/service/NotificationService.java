package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.Notification;
import com.leviathan.bidpass.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    public List<Notification> findForUser(Integer userId) {
        return notificationMapper.findByUserId(userId);
    }

    public Map<String, Object> findSummary(Integer userId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("notifications", findForUser(userId));
        summary.put("unreadCount", notificationMapper.countUnread(userId));
        return summary;
    }

    public void create(Integer userId, String type, String title, String message, Long relatedId) {
        if (userId == null) return;
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);
    }

    public void markRead(Long id, Integer userId) {
        notificationMapper.markRead(id, userId);
    }

    public void markAllRead(Integer userId) {
        notificationMapper.markAllRead(userId);
    }
}
