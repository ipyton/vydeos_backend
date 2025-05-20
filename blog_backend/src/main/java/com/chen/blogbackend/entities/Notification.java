package com.chen.blogbackend.entities;

public class Notification {
    private String userId;
    private String content;
    private String type;
    private String time;

    public String getType() {
        return type;
    }

    public Notification(String userId, String content, String type, String time) {
        this.userId = userId;
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Notification(String userId, String content, String time) {
        this.userId = userId;
        this.content = content;
        this.time = time;
    }

    public Notification() {
    }
}
