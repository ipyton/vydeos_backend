package com.chen.notification.entities;

import java.time.Instant;

public class UnreadMessage {
    private String userId;
    private String senderId;
    private String type;
    private String messageType;
    private String content;
    private Instant sendTime;
    private long messageId;
    private int count;
    private long sessionMessageId;
    private long groupId;

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    // Default constructor
    public UnreadMessage() {}

    // Constructor with all fields
    public UnreadMessage(String userId, String senderId, String type, String messageType,
                         String content, Instant sendTime, long messageId, int count, long sessionMessageId) {

        this.userId = userId;
        this.senderId = senderId;
        this.type = type;
        this.messageType = messageType;
        this.content = content;
        this.sendTime = sendTime;
        this.messageId = messageId;
        this.count = count;
        this.sessionMessageId = sessionMessageId;
    }

    public long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
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

    public Instant getSendTime() {
        return sendTime;
    }

    public void setSendTime(Instant sendTime) {
        this.sendTime = sendTime;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
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
                ", sendTime=" + sendTime +
                ", messageId=" + messageId +
                '}';
    }
}
