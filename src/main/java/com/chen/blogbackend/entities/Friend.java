package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
public class Friend {
    String userId = "";
    String avatar = "";
    String introduction = "";
    String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String groupId = "";
    boolean biDirection = false;


    public String getGroupId() {
        return groupId;
    }

    public Friend(String userId, String avatar, String introduction, String groupId, boolean biDirection) {
        this.userId = userId;
        this.avatar = avatar;
        this.introduction = introduction;
        this.groupId = groupId;
        this.biDirection = biDirection;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isBiDirection() {
        return biDirection;
    }

    public void setBiDirection(boolean biDirection) {
        this.biDirection = biDirection;
    }

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


    public Friend(String userId, String avatar, String introduction, String[] apps) {
        this.userId = userId;
        this.avatar = avatar;
        this.introduction = introduction;
    }


}
