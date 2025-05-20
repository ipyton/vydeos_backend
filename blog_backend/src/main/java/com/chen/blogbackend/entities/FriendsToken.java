package com.chen.blogbackend.entities;

import java.util.List;

public class FriendsToken {
    String userId;
    List<String> friendsId;

    public FriendsToken(String userId, List<String> friendsId) {
        this.userId = userId;
        this.friendsId = friendsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFriendsId() {
        return friendsId;
    }

    public void setFriendsId(List<String> friendsId) {
        this.friendsId = friendsId;
    }
}
