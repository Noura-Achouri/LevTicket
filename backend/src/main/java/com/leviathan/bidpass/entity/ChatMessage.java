package com.leviathan.bidpass.entity;

import java.time.LocalDateTime;

/**
 * One private message between two users of the marketplace.
 *
 * <p>{@code ticketId} is optional context ("this message is about that listing"),
 * so the UI can link the conversation back to the auction it started from.</p>
 */
public class ChatMessage {
    private Long id;
    private Integer ticketId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private Boolean read;
    private LocalDateTime createTime;

    /* ---- display-only enrichments joined in for the API response ---- */
    private String senderUsername;
    private String senderAvatar;
    private String receiverUsername;
    private String receiverAvatar;
    private String ticketEventName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }
    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }
    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    public String getReceiverAvatar() { return receiverAvatar; }
    public void setReceiverAvatar(String receiverAvatar) { this.receiverAvatar = receiverAvatar; }
    public String getTicketEventName() { return ticketEventName; }
    public void setTicketEventName(String ticketEventName) { this.ticketEventName = ticketEventName; }
}
