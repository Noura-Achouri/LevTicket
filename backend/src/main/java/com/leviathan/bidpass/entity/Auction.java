package com.leviathan.bidpass.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction {

    private Integer id;
    private Integer ticketId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal currentBid;
    private Integer winnerId;
    private String winnerUsername;
    private String status;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getCurrentBid() { return currentBid; }
    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }

    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}