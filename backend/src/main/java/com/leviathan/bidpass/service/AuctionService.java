package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.Auction;
import com.leviathan.bidpass.entity.Bid;
import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.mapper.AuctionMapper;
import com.leviathan.bidpass.mapper.BidMapper;
import com.leviathan.bidpass.mapper.OrderMapper;
import com.leviathan.bidpass.mapper.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.leviathan.bidpass.entity.Order;


@Service
public class AuctionService {

    @Autowired
    private AuctionMapper auctionMapper;

    @Autowired
    private BidMapper bidMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WatchlistService watchlistService;

    public Auction createAuction(Integer ticketId, BigDecimal startPrice, int durationHours) {
        Auction auction = new Auction();
        auction.setTicketId(ticketId);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusHours(durationHours));
        auction.setCurrentBid(startPrice);
        auctionMapper.insert(auction);
        return auction;
    }

    public Auction getAuctionByTicketId(Integer ticketId) {
        return auctionMapper.findByTicketId(ticketId);
    }

    public Auction getAuctionById(Integer auctionId) {
        return auctionMapper.findById(auctionId);
    }

    public List<Bid> getBidHistory(Integer auctionId) {
        return bidMapper.findByAuctionId(auctionId);
    }

    @Transactional
    public Auction placeBid(Integer auctionId, Integer bidderId, BigDecimal amount) {
        Auction auction = auctionMapper.findById(auctionId);

        if (auction == null) {
            throw new RuntimeException("Auction not found");
        }

        if (!auction.getStatus().equals("ongoing")) {
            throw new RuntimeException("Auction is not active");
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            closeExpiredAuction(auction);
            throw new RuntimeException("Auction has ended");
        }

        Ticket ticket = ticketMapper.findById(auction.getTicketId());
        if (ticket.getSellerId().equals(bidderId)) {
            throw new RuntimeException("Seller cannot bid on their own ticket");
        }

        if (amount.compareTo(auction.getCurrentBid()) <= 0) {
            throw new RuntimeException("Bid must exceed current bid of " + auction.getCurrentBid());
        }

        Bid previousTop = bidMapper.findHighestBid(auctionId);

        Bid bid = new Bid();
        bid.setAuctionId(auctionId);
        bid.setBidderId(bidderId);
        bid.setAmount(amount);
        bidMapper.insert(bid);

        auctionMapper.updateCurrentBid(auctionId, amount, bidderId);

        if (previousTop != null && previousTop.getBidderId() != null
                && !previousTop.getBidderId().equals(bidderId)) {
            // Someone just lost the lead - tell them, so they can respond.
            notificationService.create(previousTop.getBidderId(), "outbid", "You've been outbid",
                    "A higher bid of " + amount + " was placed on \"" + ticket.getEventName() + "\".",
                    auctionId.longValue());
        } else if (previousTop == null) {
            notificationService.create(ticket.getSellerId(), "bid", "First bid on your listing",
                    "Your ticket \"" + ticket.getEventName() + "\" just received its first bid of " + amount + ".",
                    auctionId.longValue());
        }

        auction.setCurrentBid(amount);
        auction.setWinnerId(bidderId);
        return auction;
    }

    private void closeExpiredAuction(Auction auction) {
        settleAuction(auction);
    }

    /** Closes an auction, marks the ticket sold and creates the order for the winner. */
    private void settleAuction(Auction auction) {
        Bid highestBid = bidMapper.findHighestBid(auction.getId());
        Integer winnerId = highestBid != null ? highestBid.getBidderId() : null;
        auctionMapper.closeAuction(auction.getId(), winnerId);

        if (winnerId == null) {
            return; // no bids - ticket stays available to the seller
        }

        Ticket ticket = ticketMapper.findById(auction.getTicketId());
        if (ticket != null) {
            ticketMapper.updateStatus(ticket.getId(), "sold");
        }

        if (orderMapper.findByAuctionId(auction.getId()) == null) {
            Order order = new Order();
            order.setAuctionId(auction.getId());
            order.setBuyerId(winnerId);
            order.setSellerId(ticket.getSellerId());
            order.setFinalPrice(highestBid.getAmount());
            orderMapper.insert(order);
                notificationService.create(winnerId, "order", "You won an auction",
                    "Your ticket is ready to review in Order History.", order.getId() != null ? order.getId().longValue() : null);
                notificationService.create(ticket.getSellerId(), "order", "Auction closed",
                    "The auction closed and an order was created.", order.getId() != null ? order.getId().longValue() : null);
        }

        // Everyone who saved this ticket gets a nudge when the auction resolves.
        for (Integer watcherId : watchlistService.findWatcherIds(auction.getTicketId())) {
            if (watcherId == null || watcherId.equals(winnerId)) continue;
            notificationService.create(watcherId, "auction", "Auction ended",
                    "\"" + ticket.getEventName() + "\" is no longer accepting bids.",
                    auction.getId().longValue());
        }
    }

    /**
     * Settles every auction whose end time has passed. Driven by the scheduler, so
     * auctions resolve themselves even when nobody has the page open.
     */
    @Transactional
    public int closeExpiredAuctions() {
        List<Auction> expired = auctionMapper.findExpiredOngoing();
        int closed = 0;
        for (Auction auction : expired) {
            try {
                settleAuction(auction);
                closed++;
            } catch (Exception e) {
                System.err.println("[AuctionService] failed to settle auction "
                        + auction.getId() + ": " + e.getMessage());
            }
        }
        return closed;
    }

    /** Seller manually closes the auction early and hands the ticket to the highest bidder. */
    @Transactional
    public Map<String, Object> closeBySeller(Integer auctionId, Integer sellerId) {
        Auction auction = auctionMapper.findById(auctionId);
        if (auction == null) {
            throw new RuntimeException("Auction not found");
        }
        if (!auction.getStatus().equals("ongoing")) {
            throw new RuntimeException("Auction is not active");
        }

        Ticket ticket = ticketMapper.findById(auction.getTicketId());
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }
        if (!ticket.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can close this auction");
        }

        settleAuction(auction);

        Bid winnerBid = bidMapper.findHighestBid(auctionId);
        Map<String, Object> result = new HashMap<>();
        result.put("winnerId", winnerBid != null ? winnerBid.getBidderId() : null);
        result.put("winningBid", winnerBid != null ? winnerBid.getAmount() : null);
        return result;
    }
}