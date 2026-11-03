package com.leviathan.bidpass.service;

import com.leviathan.bidpass.dto.ChatConversation;
import com.leviathan.bidpass.entity.ChatMessage;
import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.entity.User;
import com.leviathan.bidpass.mapper.ChatMapper;
import com.leviathan.bidpass.mapper.TicketMapper;
import com.leviathan.bidpass.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private static final int MAX_LENGTH = 1000;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private NotificationService notificationService;

    public List<ChatConversation> findConversations(Integer userId) {
        return chatMapper.findConversations(userId);
    }

    public int countUnread(Integer userId) {
        return chatMapper.countUnread(userId);
    }

    /** Public profile bits of the person on the other side of the thread. */
    public User findPartner(Integer partnerId) {
        return partnerId == null ? null : userMapper.findById(partnerId);
    }

    /**
     * Reading a thread also marks it as read - the user is looking at it, so the
     * unread badge should drop immediately.
     */
    @Transactional
    public List<ChatMessage> openThread(Integer userId, Integer partnerId) {
        List<ChatMessage> messages = chatMapper.findThread(userId, partnerId);
        chatMapper.markThreadRead(userId, partnerId);
        return messages;
    }

    public int markRead(Integer userId, Integer partnerId) {
        return chatMapper.markThreadRead(userId, partnerId);
    }

    @Transactional
    public ChatMessage send(Integer senderId, Integer receiverId, Integer ticketId, String content) {
        if (receiverId == null) {
            throw new RuntimeException("Recipient is required");
        }
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("You cannot message yourself");
        }
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }
        if (text.length() > MAX_LENGTH) {
            text = text.substring(0, MAX_LENGTH);
        }

        User sender = userMapper.findById(senderId);
        if (sender == null) {
            throw new RuntimeException("Sender not found");
        }
        User receiver = userMapper.findById(receiverId);
        if (receiver == null) {
            throw new RuntimeException("Recipient not found");
        }

        // Validate the optional listing context so a stale id can't break the FK.
        Integer validTicketId = null;
        String eventName = null;
        if (ticketId != null) {
            Ticket ticket = ticketMapper.findById(ticketId);
            if (ticket != null) {
                validTicketId = ticket.getId();
                eventName = ticket.getEventName();
            }
        }

        // Only ping the bell for the first unread message of a burst, so a fast
        // back-and-forth doesn't flood the notification list.
        boolean firstUnread = chatMapper.countUnreadFrom(receiverId, senderId) == 0;

        ChatMessage message = new ChatMessage();
        message.setTicketId(validTicketId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(text);
        chatMapper.insert(message);

        if (firstUnread) {
            String preview = text.length() > 120 ? text.substring(0, 120) + "…" : text;
            String title = eventName != null ? "New message · " + eventName : "New message";
            // related_id carries the sender so the bell can jump straight into the chat.
            notificationService.create(receiverId, "message", title,
                    sender.getUsername() + ": " + preview,
                    senderId.longValue());
        }

        return chatMapper.findById(message.getId());
    }
}
