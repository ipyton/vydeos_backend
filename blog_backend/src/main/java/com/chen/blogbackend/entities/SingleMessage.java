package com.chen.blogbackend.entities;

import java.time.Instant;

public class SingleMessage {
    private String avatar;
    private String userId1;
    private String userId2;
    private String receiverName;
    private String senderName;
    private String type;
    private String content;
    private Instant time;
    private long messageId;
    private long referMessageId;
    private String messageType;
    private boolean direction;
    private boolean deleted = false;
    private Long sessionMessageId ;


    public SingleMessage(String avatar, String userId1, String userId2, String receiverName, String senderName, String type, String content, Instant time, long messageId, long referMessageId, String messageType, boolean direction, boolean deleted, Long sessionMessageId) {
        this.avatar = avatar;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.receiverName = receiverName;
        this.senderName = senderName;
        this.type = type;
        this.content = content;
        this.time = time;
        this.messageId = messageId;
        this.referMessageId = referMessageId;
        this.messageType = messageType;
        this.direction = direction;
        this.deleted = deleted;
        this.sessionMessageId = sessionMessageId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(Long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }

    public boolean getDirection() {
        return direction;
    }

    public void setDirection(boolean direction) {
        this.direction = direction;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
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

    public SingleMessage(String avatar, String name, String title, String content, Instant time) {
        this.avatar = avatar;
        this.senderName = name;
        this.type = title;
        this.content = content;
        this.time = time;
    }

    public SingleMessage() {


    }
}
