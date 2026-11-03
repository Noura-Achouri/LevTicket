package com.leviathan.bidpass.entity;

import java.time.LocalDateTime;

public class Review {

    private Integer id;
    private Integer orderId;
    private Integer reviewerId;
    private Integer revieweeId;
    private Integer starRating;
    private String comment;
    private LocalDateTime createTime;
    private String reviewerUsername;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }

    public Integer getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Integer revieweeId) { this.revieweeId = revieweeId; }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getReviewerUsername() { return reviewerUsername; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }
}