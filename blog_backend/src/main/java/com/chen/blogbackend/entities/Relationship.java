package com.chen.blogbackend.entities;

public class Relationship {
    private String user_id;
    private String friend_id;
    private String avatar;
    private String group_id;
    private String name;

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public String getFriendId() {
        return friend_id;
    }

    public void setFriendId(String friend_id) {
        this.friend_id = friend_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGroupId() {
        return group_id;
    }

    public void setGroupId(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Relationship() {
    }

    public Relationship(String user_id, String friend_id, String avatar, String group_id, String name) {
        this.user_id = user_id;
        this.friend_id = friend_id;
        this.avatar = avatar;
        this.group_id = group_id;
        this.name = name;
    }
}
