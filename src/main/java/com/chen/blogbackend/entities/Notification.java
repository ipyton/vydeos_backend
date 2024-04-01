package com.chen.blogbackend.entities;

public class Notification {
    private String avatar;
    private String userId;
    private String title;
    private String content;
    private String type;
    private String time;

    public String getType() {
        return type;
    }

    public Notification(String avatar, String userId, String title, String content, String type, String time) {
        this.avatar = avatar;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Notification(String avatar, String userId, String title, String content, String time) {
        this.avatar = avatar;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    public Notification() {
    }
}
