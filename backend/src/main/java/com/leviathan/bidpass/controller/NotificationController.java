package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.service.NotificationService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> getNotifications(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return Result.success(notificationService.findSummary(userId));
    }

    @PutMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        notificationService.markRead(id, userId);
        return Result.success("Notification marked as read");
    }

    @PutMapping("/read-all")
    public Result<String> markAllRead(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        notificationService.markAllRead(userId);
        return Result.success("Notifications marked as read");
    }
}
