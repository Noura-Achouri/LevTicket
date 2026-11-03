package com.leviathan.bidpass.entity;

import java.time.LocalDateTime;

public class TrustScore {

    private Integer id;
    private Integer userId;
    private Integer score = 0;
    private Integer totalAuctions = 0;
    private Integer successfulAuctions = 0;
    private Integer disputes = 0;
    private LocalDateTime lastUpdated;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalAuctions() { return totalAuctions; }
    public void setTotalAuctions(Integer totalAuctions) { this.totalAuctions = totalAuctions; }

    public Integer getSuccessfulAuctions() { return successfulAuctions; }
    public void setSuccessfulAuctions(Integer successfulAuctions) { this.successfulAuctions = successfulAuctions; }

    public Integer getDisputes() { return disputes; }
    public void setDisputes(Integer disputes) { this.disputes = disputes; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}