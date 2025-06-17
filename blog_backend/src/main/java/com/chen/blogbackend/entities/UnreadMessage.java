package com.chen.blogbackend.entities;

import java.time.Instant;

public class UnreadMessage {
    private String userId;
    private String senderId;
    private String type;
    private String messageType;
    private String content;
    private Instant timestamp;
    private Long messageId;
    private Integer count;
    private Long sessionMessageId;
    private Long groupId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    // Default constructor
    public UnreadMessage() {}

    // Constructor with all fields
    public UnreadMessage(String userId, String senderId, String type, String messageType,
                         String content, Instant timestamp, Long messageId, Integer count, Long sessionMessageId,Long groupId) {

        this.userId = userId;
        this.senderId = senderId;
        this.type = type;
        this.messageType = messageType;
        this.content = content;
        this.timestamp = timestamp;
        this.messageId = messageId;
        this.count = count;
        this.sessionMessageId = sessionMessageId;
        this.groupId = groupId;
    }

    public Long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(Long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "UnreadMessage{" +
                "userId='" + userId + '\'' +
                ", receiverId='" + senderId + '\'' +
                ", type='" + type + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", messageId=" + messageId +
                '}';
    }
}
