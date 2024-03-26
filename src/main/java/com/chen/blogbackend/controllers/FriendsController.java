package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("friends")
@Controller()
@ResponseBody
public class FriendsController {

    @Autowired
    FriendsService service;

    @Autowired
    AccountService accountService;

    @RequestMapping("follow")
    public LoginMessage follow(String fanId, String idolId){
        boolean follow = service.follow(fanId, idolId);
        return new LoginMessage(follow?1:-1,"");
    }

    @RequestMapping("unfollow")
    public LoginMessage unfollow(String fanId, String userId) {
        boolean result = service.unfollow(fanId, userId);
        return new LoginMessage(-1, "" + result);
    }

    @RequestMapping("get_followers")
    public PagingMessage<Friend> getFollowers(String userId, String pagingState) {
        return service.getFollowersByUserId(userId, pagingState);
    }

    @RequestMapping("get_idols")
    public PagingMessage<Friend> getIdols(String userId, String pagingState) {
        return service.getIdolsByUserId(userId,pagingState);
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
    public LoginMessage moveTo(String userId, String friendId,String groupId) {
        boolean b = service.moveToGroup(userId, friendId, groupId);

        return new LoginMessage(-1, "");
    }

    @RequestMapping("create_group")
    public LoginMessage createGroup(String userId, UserGroup group) {
        service.createGroup(group);
        return new LoginMessage(-1, "");
    }


    @RequestMapping("remove_group")
    public LoginMessage removeGroup(String userId, String group) {
        boolean result = service.removeGroup(group);
        if (result) {
            return new LoginMessage(1, "");
        }
        else {
            return new LoginMessage(-1, " ");
        }
    }

    @RequestMapping("delete_from_group")
    public LoginMessage deleteFromGroup(String user, String usrToRemove, String groupFrom) {
        boolean result = service.deleteFromGroup(user, usrToRemove, groupFrom);
        return new LoginMessage(-1, "");
    }

    @PostMapping("getContactDetailsById")
    public LoginMessage getContactDetailsById(String userId, String userIdToFollow) throws Exception {
        Friend friendDetailsById = accountService.getFriendDetailsById(userId, userIdToFollow);
        return new LoginMessage(-1,JSON.toJSONString(friendDetailsById));
    }




}
