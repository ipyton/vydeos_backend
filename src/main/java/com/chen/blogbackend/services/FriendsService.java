package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.UserGroupDao;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.chen.blogbackend.mappers.FriendMapper;
import com.chen.blogbackend.mappers.FriendMapperBuilder;
import com.chen.blogbackend.mappers.UserGroupMapper;
import com.chen.blogbackend.mappers.UserGroupMapperBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.protocol.internal.request.Prepare;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    FriendDao friendDao;
    UserGroupDao userGroupDao;

    PreparedStatement addFriendUserId;
    PreparedStatement addFriendByIdol;


    @PostConstruct
    public void init(){
        friendDao = new FriendMapperBuilder(session).build().getDao();
        userGroupDao = new UserGroupMapperBuilder(session).build().getDao();
        addFriendByIdol = session.prepare("");
        addFriendUserId = session.prepare("");


    }

    public List<Friend> getFollowersByUserId(String userId) {
        PagingIterable<Friend> friends = friendDao.selectUserFollowers(userId);
        return friends.all();
    }

    public List<Friend> getIdolsByUserId(String userId){
        PagingIterable<Friend> friends = friendDao.selectUserFollows(userId);
        return friends.all();
    }

    public List<String> getIdolIdsByUserId(String userId) {
        PagingIterable<String> ids = friendDao.selectUserIdsFollows(userId);
        return ids.all();
    }


    public List<Friend> getFriendsByGroupId(String userId, String groupId) {
        PagingIterable<Friend> userGroups = userGroupDao.selectFriendsByGroupId(userId, groupId);

        return userGroups.all();
    }

    public List<String> getFriendIdsByGroupId(String userId, String groupId) {
        PagingIterable<String> strings = userGroupDao.selectUserIdByGroupID(groupId);
        return strings.all();
    }

    public List<UserGroup> getGroupById(String userId) {
        PagingIterable<UserGroup> userGroups = userGroupDao.selectGroupByGroupID(userId);
        return userGroups.all();
    }

    public boolean follow(String from, String to) {


        return false;
    }



}
