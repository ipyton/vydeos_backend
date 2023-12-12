package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
public class Friend {
    String userId = "";
    String avatar = "";
    String introduction = "";
    String[] apps;

    public String getUserId() {
        return userId;
    }

    public Friend() {
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

    public Friend(String userId, String avatar, String introduction, String[] apps) {
        this.userId = userId;
        this.avatar = avatar;
        this.introduction = introduction;
        this.apps = apps;
    }


}
