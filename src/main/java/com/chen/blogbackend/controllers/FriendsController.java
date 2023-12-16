package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller("friends")
public class FriendsController {

    @Autowired
    FriendsService service;

    @RequestMapping("follow")
    public LoginMessage follow(String fanId, String idolId){
        boolean follow = service.follow(fanId, idolId);
        return new LoginMessage(follow?1:-1,"");
    }

    @RequestMapping("unfollow")
    public LoginMessage unfollow(String userId) {

    }

    @RequestMapping("get_followers")
    public LoginMessage getFollowers(String userId) {

    }

    @RequestMapping("get_idols")
    public LoginMessage getIdols(String userId) {
    }

    @RequestMapping("get_groups")
    public PagingMessage<UserGroup> getGroups(String userId) {
        List<UserGroup> groupById = service.getGroupById(userId);
        PagingMessage<UserGroup> pagingMessage = new PagingMessage<>();
        pagingMessage.items = groupById;
        return pagingMessage;
    }

    @RequestMapping("get_group_users")
    public PagingMessage<Friend> getFriends(String userId,String groupId) {
        List<Friend> friendIdsByGroupId = service.getFriendsByGroupId(userId, groupId);
        PagingMessage<Friend> message = new PagingMessage<>();
        message.items = friendIdsByGroupId;
        return message;
    }


    @RequestMapping("move_to_group")
    public LoginMessage moveTo(String userId, String groupId) {

    }

    @RequestMapping("create_group")
    public LoginMessage createGroup(String userId, String group) {

    }


    @RequestMapping("")
    public LoginMessage c



}
