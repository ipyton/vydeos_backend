package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.Date;

@Entity
public class SingleMessage {
    private String messageId;
    private String userId;
    private String receiverId;
    private String type;
    private Date timestamp;
    private String content;
    private String media;
    private String referMessageId;

    public SingleMessage() {
    }

    public SingleMessage(String messageId, String userId, String receiverId, String type, Date timestamp, String content, String media, String referMessageId) {
        this.messageId = messageId;
        this.userId = userId;
        this.receiverId = receiverId;
        this.type = type;
        this.timestamp = timestamp;
        this.content = content;
        this.media = media;
        this.referMessageId = referMessageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getReferMessageId() {
        return referMessageId;
    }

    public void setReferMessageId(String referMessageId) {
        this.referMessageId = referMessageId;
    }
}
