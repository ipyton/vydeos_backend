package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.FriendsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller()
@ResponseBody
@RequestMapping("friends")
public class FriendsController {

    @Autowired
    FriendsService service;

    @Autowired
    AccountService accountService;


    @RequestMapping("getFollowState")
    public LoginMessage getFollowState(String sender, String receiver) throws Exception {
        System.out.println(sender + receiver);
        return new LoginMessage(1, Integer.toString(service.getRelationship(sender, receiver)));
    }


    @RequestMapping("follow")
    public LoginMessage follow(String sender, String receiver){
        System.out.println(sender + receiver);
        boolean follow = service.follow(sender, receiver);
        return new LoginMessage(follow?1:-1,"");
    }

    @RequestMapping("unfollow")
    public LoginMessage unfollow(String sender, String receiver) {
        System.out.println(sender + receiver);
        boolean result = service.unfollow(sender, receiver);
        return new LoginMessage(result?1:-1, "" );
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

    //get user introduction from searching/friend list
    @PostMapping("getUserIntro")
    public LoginMessage getIntro(HttpServletRequest request,String userIdToFollow) throws Exception {
        String userId = (String) request.getAttribute("userEmail");
        Account friendDetailsById = accountService.getFriendDetailsById(userId, userIdToFollow);
        if (null == friendDetailsById ) {
            return new LoginMessage(-1, null);
        }
        System.out.println(JSON.toJSONString(friendDetailsById));
        return new LoginMessage(1,JSON.toJSONString(friendDetailsById));
    }

    //This is a method to call if user doesn't have local messages.
    @PostMapping("getFriendsToken")
    public LoginMessage getFriendsToken(String user) {
        //service.getRelationship()
        return new LoginMessage(1, "success");
    }


}
