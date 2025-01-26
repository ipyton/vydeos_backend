package com.chen.notification.entities;

import java.time.Instant;

public class NotificationMessage {
    private String avatar;
    private String senderId;
    private String receiverId;
    private long groupId;
    private String receiverName;
    private String senderName;
    private String type;
    private String content;
    private Instant time;
    private long messageId;
    private long referMessageId;
    private String messageType;


    public String getReceiverName() {
        return receiverName;
    }

    public NotificationMessage(String avatar, String senderId, String receiverId, long groupId, String receiverName, String senderName,
                               String type, String content, long messageId, Instant time, long referMessageId) {
        this.avatar = avatar;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.senderName = senderName;
        this.type = type;
        this.content = content;
        this.time = time;
        this.messageId = messageId;
        this.referMessageId = referMessageId;
        this.groupId = groupId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getReferMessageId() {
        return referMessageId;
    }

    public void setReferMessageId(long referMessageId) {
        this.referMessageId = referMessageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

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
