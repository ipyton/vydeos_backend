package com.chen.notification.entities;

import java.time.Instant;

public class GroupMessage {
    private long messageId;
    private long userId;
    private long groupId;
    private String type;
    private Instant timestamp;
    private String content;
    private String media;
    private long referUserId;
    private long referMessageId;
    private boolean recall;

    public GroupMessage(boolean recall, long referMessageId, long referUserId, String media, String content, Instant timestamp, String type, long groupId, long userId, long messageId) {
        this.recall = recall;
        this.referMessageId = referMessageId;
        this.referUserId = referUserId;
        this.media = media;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.groupId = groupId;
        this.userId = userId;
        this.messageId = messageId;
    }

    public boolean isRecall() {
        return recall;
    }

    public void setRecall(boolean recall) {
        this.recall = recall;
    }

    public long getReferUserId() {
        return referUserId;
    }

    public void setReferUserId(long referUserId) {
        this.referUserId = referUserId;
    }

    public long getReferMessageId() {
        return referMessageId;
    }

    public void setReferMessageId(long referMessageId) {
        this.referMessageId = referMessageId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
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

    public GroupMessage(long messageId, long userId, String type, Instant timestamp, String content, String media) {
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
