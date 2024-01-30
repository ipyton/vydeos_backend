package com.chen.notification.entities;

public class Notification {
    private String avatar;
    private String name;
    private String title;
    private String content;
    private String time;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Notification(String avatar, String name, String title, String content, String time) {
        this.avatar = avatar;
        this.name = name;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    public Notification() {
    }
}
