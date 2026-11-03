package com.leviathan.bidpass.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid {

    private Integer id;
    private Integer auctionId;
    private Integer bidderId;
    private String bidderUsername;
    private BigDecimal amount;
    private LocalDateTime bidTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAuctionId() { return auctionId; }
    public void setAuctionId(Integer auctionId) { this.auctionId = auctionId; }

    public Integer getBidderId() { return bidderId; }
    public void setBidderId(Integer bidderId) { this.bidderId = bidderId; }

    public String getBidderUsername() { return bidderUsername; }
    public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
}