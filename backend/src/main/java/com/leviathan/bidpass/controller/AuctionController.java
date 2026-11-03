package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.Auction;
import com.leviathan.bidpass.entity.Bid;
import com.leviathan.bidpass.service.AuctionService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @GetMapping("/{id}")
    public Result<Auction> getAuction(@PathVariable Integer id) {
        Auction auction = auctionService.getAuctionById(id);
        if (auction == null) {
            return Result.error(404, "Auction not found");
        }
        return Result.success(auction);
    }

    @GetMapping("/ticket/{ticketId}")
    public Result<Auction> getAuctionByTicket(@PathVariable Integer ticketId) {
        Auction auction = auctionService.getAuctionByTicketId(ticketId);
        if (auction == null) {
            return Result.error(404, "Auction not found for this ticket");
        }
        return Result.success(auction);
    }

    @GetMapping("/{id}/bids")
    public Result<List<Bid>> getBidHistory(@PathVariable Integer id) {
        List<Bid> bids = auctionService.getBidHistory(id);
        return Result.success(bids);
    }

    @PostMapping("/{id}/bids")
    public Result<Map<String, Object>> placeBid(
            @PathVariable Integer id,
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {

        Integer bidderId = (Integer) request.getAttribute("userId");
        if (bidderId == null) {
            return Result.error(401, "Unauthorized");
        }

        if (body.get("amount") == null) {
            return Result.error(400, "Bid amount is required");
        }

        try {
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            Auction auction = auctionService.placeBid(id, bidderId, amount);

            Map<String, Object> data = new HashMap<>();
            data.put("newCurrentBid", auction.getCurrentBid());
            data.put("auctionEndTime", auction.getEndTime());
            data.put("winnerId", auction.getWinnerId());
            return Result.success(data);

        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/close")
    public Result<Map<String, Object>> closeAuction(
            @PathVariable Integer id,
            HttpServletRequest request) {

        Integer sellerId = (Integer) request.getAttribute("userId");
        if (sellerId == null) {
            return Result.error(401, "Unauthorized");
        }

        try {
            Map<String, Object> result = auctionService.closeBySeller(id, sellerId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}