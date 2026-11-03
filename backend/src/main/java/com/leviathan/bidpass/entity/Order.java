package com.leviathan.bidpass.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {

    private Integer id;
    private Integer auctionId;
    private Integer ticketId;
    private Integer buyerId;
    private Integer sellerId;
    private BigDecimal finalPrice;
    private String status;
    private LocalDateTime createTime;
    private String ticketEventName;
    private String sellerUsername;
    private Boolean reviewLeft;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAuctionId() { return auctionId; }
    public void setAuctionId(Integer auctionId) { this.auctionId = auctionId; }

    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }

    public Integer getBuyerId() { return buyerId; }
    public void setBuyerId(Integer buyerId) { this.buyerId = buyerId; }

    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getTicketEventName() { return ticketEventName; }
    public void setTicketEventName(String ticketEventName) { this.ticketEventName = ticketEventName; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public Boolean getReviewLeft() { return reviewLeft; }
    public void setReviewLeft(Boolean reviewLeft) { this.reviewLeft = reviewLeft; }
}