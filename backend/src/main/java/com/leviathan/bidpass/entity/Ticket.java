package com.leviathan.bidpass.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Ticket {

    private Integer id;
    private Integer sellerId;
    private String sellerUsername;
    private Integer categoryId;
    private String eventName;
    private LocalDate eventDate;
    private String eventLocation;
    private String seatInfo;
    private String description;
    private BigDecimal startPrice;
    private String status;
    private String auctionStatus;
    private LocalDateTime auctionEndTime;
    private String tags;
    private LocalDateTime createTime;
    private List<String> imageUrls;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public String getSeatInfo() { return seatInfo; }
    public void setSeatInfo(String seatInfo) { this.seatInfo = seatInfo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getStartPrice() { return startPrice; }
    public void setStartPrice(BigDecimal startPrice) { this.startPrice = startPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAuctionStatus() { return auctionStatus; }
    public void setAuctionStatus(String auctionStatus) { this.auctionStatus = auctionStatus; }

    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}