package com.leviathan.bidpass.ai.dto;
import lombok.Data;

@Data
public class TicketExtractDTO {
    private String eventName;
    private String eventDate;
    private String venue;
    private String seatNumber;
    private String ticketType;
    private Double originalPrice;
    private String suggestedSellPrice;
    private String listingTips;
}
