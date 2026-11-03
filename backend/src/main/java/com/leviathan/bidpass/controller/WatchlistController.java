package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.service.WatchlistService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Saved / watched tickets. Every endpoint here sits behind the JWT interceptor,
 * so the authenticated user id is always present on the request.
 */
@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping
    public Result<List<Ticket>> getWatchlist(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return Result.success(watchlistService.findWatchedTickets(userId));
    }

    /** Lightweight list of ids so the UI can render heart state without N requests. */
    @GetMapping("/ids")
    public Result<List<Integer>> getWatchedIds(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return Result.success(watchlistService.findWatchedTicketIds(userId));
    }

    @PostMapping("/{ticketId}")
    public Result<Map<String, Object>> add(HttpServletRequest request, @PathVariable Integer ticketId) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        try {
            watchlistService.add(userId, ticketId);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
        return Result.success(state(userId, ticketId, true));
    }

    @DeleteMapping("/{ticketId}")
    public Result<Map<String, Object>> remove(HttpServletRequest request, @PathVariable Integer ticketId) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        try {
            watchlistService.remove(userId, ticketId);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
        return Result.success(state(userId, ticketId, false));
    }

    private Map<String, Object> state(Integer userId, Integer ticketId, boolean fallback) {
        Map<String, Object> data = new HashMap<>();
        data.put("watching", watchlistService.isWatching(userId, ticketId));
        data.put("count", watchlistService.countForTicket(ticketId));
        data.put("requested", fallback);
        return data;
    }
}
