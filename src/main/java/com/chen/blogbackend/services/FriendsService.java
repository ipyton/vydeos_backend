package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.protocol.internal.request.Prepare;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.ArrayList;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    PreparedStatement getFriendsByUser;
    PreparedStatement getFriendsByGroup;
    PreparedStatement getGroups;

    PreparedStatement addFriends;
    PreparedStatement addGroup;


    PreparedStatement deleteGroup;
    PreparedStatement deleteFriend;



    @PostConstruct
    public void init(){

    }


    public ArrayList<Friend> getFriendsByUserId(String userId) {
        ArrayList<Friend> friends = new ArrayList<>();
        return friends;
    }



    public ArrayList<Friend> getFriendsByGroupId(String userId, String groupId) {
        ArrayList<Friend> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<String> getFriendIdsByGroupId(String userId, String groupId) {
        ArrayList<String> friendIds = new ArrayList<>();
        return friendIds;
    }

    public ArrayList<UserGroup> getGroupById(String userId) {
        ArrayList<UserGroup> userGroups = new ArrayList<>();
        return userGroups;
    }

    public void setFriends() {

    }


}
