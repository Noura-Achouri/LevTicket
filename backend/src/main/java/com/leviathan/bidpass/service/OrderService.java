package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.Auction;
import com.leviathan.bidpass.entity.Order;
import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.mapper.AuctionMapper;
import com.leviathan.bidpass.mapper.OrderMapper;
import com.leviathan.bidpass.mapper.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AuctionMapper auctionMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TrustScoreService trustScoreService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Order createOrderFromAuction(Integer auctionId) {
        Auction auction = auctionMapper.findById(auctionId);

        if (auction == null) {
            throw new RuntimeException("Auction not found");
        }
        if (auction.getWinnerId() == null) {
            throw new RuntimeException("Auction has no winner yet");
        }
        if (orderMapper.findByAuctionId(auctionId) != null) {
            throw new RuntimeException("Order already exists for this auction");
        }

        Ticket ticket = ticketMapper.findById(auction.getTicketId());

        Order order = new Order();
        order.setAuctionId(auctionId);
        order.setBuyerId(auction.getWinnerId());
        order.setSellerId(ticket.getSellerId());
        order.setFinalPrice(auction.getCurrentBid());

        orderMapper.insert(order);
        return order;
    }

    public Order findById(Integer id) {
        return orderMapper.findById(id);
    }

    public Order findByAuctionId(Integer auctionId) {
        return orderMapper.findByAuctionId(auctionId);
    }

    public List<Order> findByBuyerId(Integer buyerId) {
        return orderMapper.findByBuyerId(buyerId);
    }

    public List<Order> findBySellerId(Integer sellerId) {
        return orderMapper.findBySellerId(sellerId);
    }

    public void updateStatus(Integer orderId, Integer userId, String status) {
    Order order = orderMapper.findById(orderId);
    if (order == null) {
        throw new RuntimeException("Order not found");
    }
    if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
        throw new RuntimeException("You are not a party to this order");
    }

    if (status.equals("completed") && !order.getBuyerId().equals(userId)) {
        throw new RuntimeException("Only the buyer can confirm ticket receipt");
    }
    if (!status.equals("completed") && !status.equals("disputed") && !status.equals("confirmed") && !status.equals("pending")) {
        throw new RuntimeException("Invalid order status");
    }

    orderMapper.updateStatus(orderId, status);

    if (status.equals("confirmed")) {
        notificationService.create(order.getSellerId(), "order", "Ticket received",
                "The buyer confirmed the ticket was valid.", orderId.longValue());
    }

    if (status.equals("completed")) {
        trustScoreService.updateAfterCompletedOrder(order.getBuyerId(), false);
        trustScoreService.updateAfterCompletedOrder(order.getSellerId(), false);
    }

    if (status.equals("disputed")) {
        trustScoreService.updateAfterDispute(order.getBuyerId());
        trustScoreService.updateAfterDispute(order.getSellerId());
    }
}
}