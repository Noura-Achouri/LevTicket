package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.ChatMessage;
import com.leviathan.bidpass.entity.User;
import com.leviathan.bidpass.service.ChatService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Private buyer &lt;-&gt; seller messaging. Every route lives behind the JWT
 * interceptor (it sits under {@code /api/**}), so both sides are always known.
 */
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /** Inbox: one entry per person you have talked to, plus the global unread badge. */
    @GetMapping
    public Result<Map<String, Object>> inbox(HttpServletRequest request) {
        Integer userId = userId(request);
        if (userId == null) return Result.error(401, "Unauthorized");

        Map<String, Object> data = new HashMap<>();
        data.put("conversations", chatService.findConversations(userId));
        data.put("unreadCount", chatService.countUnread(userId));
        return Result.success(data);
    }

    /** Cheap endpoint the header polls just for the unread badge. */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(HttpServletRequest request) {
        Integer userId = userId(request);
        if (userId == null) return Result.error(401, "Unauthorized");

        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", chatService.countUnread(userId));
        return Result.success(data);
    }

    /** Full thread with one user. Reading it marks the partner's messages as read. */
    @GetMapping("/{partnerId}/messages")
    public Result<Map<String, Object>> thread(@PathVariable Integer partnerId, HttpServletRequest request) {
        Integer userId = userId(request);
        if (userId == null) return Result.error(401, "Unauthorized");

        Map<String, Object> data = new HashMap<>();
        data.put("messages", chatService.openThread(userId, partnerId));
        data.put("unreadCount", chatService.countUnread(userId));

        User partner = chatService.findPartner(partnerId);
        if (partner != null) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", partner.getId());
            info.put("username", partner.getUsername());
            info.put("avatarUrl", partner.getAvatarUrl());
            data.put("partner", info);
        }
        return Result.success(data);
    }

    @PostMapping("/{partnerId}/messages")
    public Result<Map<String, Object>> send(@PathVariable Integer partnerId,
                                            @RequestBody Map<String, Object> body,
                                            HttpServletRequest request) {
        Integer userId = userId(request);
        if (userId == null) return Result.error(401, "Unauthorized");

        Integer ticketId = parseId(body.get("ticketId"));
        try {
            ChatMessage message = chatService.send(userId, partnerId, ticketId, asText(body.get("content")));
            Map<String, Object> data = new HashMap<>();
            data.put("message", message);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{partnerId}/read")
    public Result<Map<String, Object>> markRead(@PathVariable Integer partnerId, HttpServletRequest request) {
        Integer userId = userId(request);
        if (userId == null) return Result.error(401, "Unauthorized");

        chatService.markRead(userId, partnerId);
        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", chatService.countUnread(userId));
        return Result.success(data);
    }

    private Integer userId(HttpServletRequest request) {
        Object value = request.getAttribute("userId");
        return value instanceof Integer ? (Integer) value : null;
    }

    private String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer parseId(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
