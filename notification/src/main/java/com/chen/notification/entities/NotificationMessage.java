package com.chen.notification.entities;

import java.time.Instant;

public class NotificationMessage {
    private String avatar;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String type;
    private String messageType;
    private String content;
    private Instant time;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public NotificationMessage(String avatar, String senderId, String receiverId, String senderName, String messageType, String type, String content, Instant time) {
        this.avatar = avatar;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.type = type;
        this.content = content;
        this.time = time;
        this.messageType = messageType;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public NotificationMessage(String avatar, String name, String title, String content, Instant time) {
        this.avatar = avatar;
        this.senderName = name;
        this.type = title;
        this.content = content;
        this.time = time;
    }

    public NotificationMessage() {


    }
}
