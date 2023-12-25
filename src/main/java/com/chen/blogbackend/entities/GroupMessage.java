package com.chen.blogbackend.entities;

import java.util.Date;

public class GroupMessage {
    private String messageId;
    private String userId;
    private String groupId;
    private String type;
    private Date timestamp;
    private String content;
    private String media;
    private String referUserId;
    private String referMessageId;

    public GroupMessage(String messageId, String userId, String groupId, String type, Date timestamp, String content, String media, String referUserId, String referMessageId) {
        this.messageId = messageId;
        this.userId = userId;
        this.groupId = groupId;
        this.type = type;
        this.timestamp = timestamp;
        this.content = content;
        this.media = media;
        this.referUserId = referUserId;
        this.referMessageId = referMessageId;
    }

    public String getReferUserId() {
        return referUserId;
    }

    public void setReferUserId(String referUserId) {
        this.referUserId = referUserId;
    }

    public String getReferMessageId() {
        return referMessageId;
    }

    public void setReferMessageId(String referMessageId) {
        this.referMessageId = referMessageId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public GroupMessage(String messageId, String userId, String type, Date timestamp, String content, String media) {
        this.messageId = messageId;
        this.userId = userId;
        this.type = type;
        this.timestamp = timestamp;
        this.content = content;
        this.media = media;
    }

    public GroupMessage() {
    }
}
