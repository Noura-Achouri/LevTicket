package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.mapper.TicketMapper;
import com.leviathan.bidpass.mapper.WatchlistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistMapper watchlistMapper;

    @Autowired
    private TicketMapper ticketMapper;

    public List<Ticket> findWatchedTickets(Integer userId) {
        return watchlistMapper.findWatchedTickets(userId);
    }

    public List<Integer> findWatchedTicketIds(Integer userId) {
        return watchlistMapper.findTicketIdsByUserId(userId);
    }

    /** Number of users watching a ticket - shown as social proof on the detail page. */
    public int countForTicket(Integer ticketId) {
        return watchlistMapper.countByTicketId(ticketId);
    }

    public boolean isWatching(Integer userId, Integer ticketId) {
        return watchlistMapper.exists(userId, ticketId) > 0;
    }

    @Transactional
    public boolean add(Integer userId, Integer ticketId) {
        if (ticketId == null) {
            throw new RuntimeException("Ticket id is required");
        }
        if (ticketMapper.findById(ticketId) == null) {
            throw new RuntimeException("Ticket not found");
        }
        watchlistMapper.insert(userId, ticketId);
        return true;
    }

    @Transactional
    public boolean remove(Integer userId, Integer ticketId) {
        if (ticketId == null) {
            throw new RuntimeException("Ticket id is required");
        }
        return watchlistMapper.delete(userId, ticketId) > 0;
    }

    /** Everyone watching a ticket - used to fan out "auction ended" notifications. */
    public List<Integer> findWatcherIds(Integer ticketId) {
        return watchlistMapper.findWatcherIdsByTicketId(ticketId);
    }
}
