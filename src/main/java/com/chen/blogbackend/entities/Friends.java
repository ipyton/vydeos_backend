package com.chen.blogbackend.entities;

public class Friends {
    String userId = "";
    String avatar = "";
    String introduction = "";
    String[] apps;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String[] getApps() {
        return apps;
    }

    public void setApps(String[] apps) {
        this.apps = apps;
    }

    public Friends(String userId, String avatar, String introduction, String[] apps) {
        this.userId = userId;
        this.avatar = avatar;
        this.introduction = introduction;
        this.apps = apps;
    }


}
