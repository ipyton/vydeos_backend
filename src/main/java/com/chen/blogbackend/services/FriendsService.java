package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Friends;
import com.chen.blogbackend.entities.UserGroup;
import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.tomcat.util.modeler.FeatureInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    public ArrayList<Friends> getFriendsByUserId(String userId) {
        ArrayList<Friends> friends = new ArrayList<>();
        return friends;
    }



    public ArrayList<Friends> getFriendsByGroupId(String userId, String groupId) {
        ArrayList<Friends> friends = new ArrayList<>();
        return friends;
    }

    public ArrayList<UserGroup> getGroupById(String userId) {
        ArrayList<UserGroup> userGroups = new ArrayList<>();
        return userGroups;
    }

    public void setFriends() {

    }


}
