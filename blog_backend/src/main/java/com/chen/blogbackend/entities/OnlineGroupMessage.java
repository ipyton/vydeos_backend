package com.chen.blogbackend.entities;

import java.time.Instant;

public class OnlineGroupMessage {
    private long userId;
    private long groupId;
    private String type;
    private String content;
    private int count;
    private Instant latestTimestamp;


    public OnlineGroupMessage(long userId, long groupId, String type, String content, int count, Instant latestTimestamp) {
        this.userId = userId;
        this.groupId = groupId;
        this.type = type;
        this.content = content;
        this.count = count;
        this.latestTimestamp = latestTimestamp;
    }

    public Instant getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(Instant latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
