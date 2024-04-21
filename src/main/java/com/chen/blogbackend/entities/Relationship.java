package com.chen.blogbackend.entities;

public class Relationship {
    private String user_id;
    private String friend_id;
    private String avatar;
    private String group_id;
    private String name;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Relationship(String user_id, String friend_id, String avatar, String group_id, String name) {
        this.user_id = user_id;
        this.friend_id = friend_id;
        this.avatar = avatar;
        this.group_id = group_id;
        this.name = name;
    }
}
