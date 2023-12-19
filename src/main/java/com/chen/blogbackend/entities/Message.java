package com.chen.blogbackend.entities;

import java.util.Date;

public class Message {
    private String messageId;
    private String userId;
    private String type;
    private Date timestamp;
    private String content;
    private String media;

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

    public Message(String messageId, String userId, String type, Date timestamp, String content, String media) {
        this.messageId = messageId;
        this.userId = userId;
        this.type = type;
        this.timestamp = timestamp;
        this.content = content;
        this.media = media;
    }

    public Message() {
    }
}
