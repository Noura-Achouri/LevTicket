package com.leviathan.bidpass.dto;

import java.time.LocalDateTime;

/**
 * A conversation summary for the Messages inbox: the other user, the last
 * message exchanged and how many messages are still unread.
 */
public class ChatConversation {
    private Integer partnerId;
    private String partnerUsername;
    private String partnerAvatar;
    private Integer ticketId;
    private String ticketEventName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    /** true when the current user sent the last message. */
    private Boolean outgoing;
    private Integer unread;

    public Integer getPartnerId() { return partnerId; }
    public void setPartnerId(Integer partnerId) { this.partnerId = partnerId; }
    public String getPartnerUsername() { return partnerUsername; }
    public void setPartnerUsername(String partnerUsername) { this.partnerUsername = partnerUsername; }
    public String getPartnerAvatar() { return partnerAvatar; }
    public void setPartnerAvatar(String partnerAvatar) { this.partnerAvatar = partnerAvatar; }
    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }
    public String getTicketEventName() { return ticketEventName; }
    public void setTicketEventName(String ticketEventName) { this.ticketEventName = ticketEventName; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public Boolean getOutgoing() { return outgoing; }
    public void setOutgoing(Boolean outgoing) { this.outgoing = outgoing; }
    public Integer getUnread() { return unread; }
    public void setUnread(Integer unread) { this.unread = unread; }
}
