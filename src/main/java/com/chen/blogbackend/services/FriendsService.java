package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    public ArrayList<Friend> getFriendsByUserId(String userId) {
        ArrayList<Friend> friends = new ArrayList<>();
        return friends;
    }



    public ArrayList<Friend> getFriendsByGroupId(String userId, String groupId) {
        ArrayList<Friend> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<UserGroup> getGroupById(String userId) {
        ArrayList<UserGroup> userGroups = new ArrayList<>();
        return userGroups;
    }

    public void setFriends() {

    }


}
