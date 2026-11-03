package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.Auction;
import com.leviathan.bidpass.entity.Order;
import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.mapper.AuctionMapper;
import com.leviathan.bidpass.mapper.OrderMapper;
import com.leviathan.bidpass.mapper.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private AuctionMapper auctionMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    @Lazy
    private AuctionService auctionService;

    public Map<String, Object> createTicket(Integer sellerId, Integer categoryId, String eventName,
                               String eventDate, String eventLocation, String seatInfo,
                               String description, BigDecimal startPrice, String tags,
                               int durationHours) {
        Ticket ticket = new Ticket();
        ticket.setSellerId(sellerId);
        ticket.setCategoryId(categoryId);
        ticket.setEventName(eventName);
        ticket.setEventDate(LocalDate.parse(eventDate));
        ticket.setEventLocation(eventLocation);
        ticket.setSeatInfo(seatInfo);
        ticket.setDescription(description);
        ticket.setStartPrice(startPrice);
        ticket.setTags(tags);

        ticketMapper.insert(ticket);

        Auction auction = auctionService.createAuction(ticket.getId(), startPrice, durationHours);

        Map<String, Object> result = new HashMap<>();
        result.put("ticketId", ticket.getId());
        result.put("auctionId", auction.getId());
        return result;
    }

    public Ticket findById(Integer id) {
        return ticketMapper.findById(id);
    }

    public Map<String, Object> findAll(String keyword, Integer categoryId, String location,
                                       String tag, String sort, int page, int size) {
        int offset = (page - 1) * size;
        if (sort == null) sort = "recent";

        List<Ticket> tickets = ticketMapper.findAll(keyword, categoryId, location, tag, sort, offset, size);
        int total = ticketMapper.countAll(keyword, categoryId, location, tag);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("tickets", tickets);
        return result;
    }

    public List<Ticket> findBySellerId(Integer sellerId, String status) {
        return ticketMapper.findBySellerId(sellerId, status);
    }

    public boolean deleteTicket(Integer ticketId, Integer sellerId) {
        return ticketMapper.deleteByIdAndSellerId(ticketId, sellerId) > 0;
    }

    public void addImage(Integer ticketId, String imageUrl) {
        ticketMapper.insertImage(ticketId, imageUrl, false);
    }

    public void addPrivateImage(Integer ticketId, String imageUrl) {
        ticketMapper.insertImage(ticketId, imageUrl, true);
    }

    public List<String> findPrivateImages(Integer ticketId) {
        return ticketMapper.findPrivateImageUrlsByTicketId(ticketId);
    }

    /**
     * The real ticket photo may only be seen by the seller, or by the buyer who
     * won the auction (i.e. has an order for it). Everyone else gets nothing.
     */
    public boolean canViewTicketPhoto(Integer ticketId, Integer userId) {
        if (ticketId == null || userId == null) return false;
        Ticket ticket = ticketMapper.findById(ticketId);
        if (ticket == null) return false;
        if (ticket.getSellerId().equals(userId)) return true;

        Auction auction = auctionMapper.findByTicketId(ticketId);
        if (auction != null) {
            Order order = orderMapper.findByAuctionId(auction.getId());
            if (order != null && order.getBuyerId().equals(userId)) return true;
        }
        return false;
    }
}